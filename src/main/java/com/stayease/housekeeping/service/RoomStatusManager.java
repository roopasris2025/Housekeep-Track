package com.stayease.housekeeping.service;


import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.stayease.housekeeping.entity.Room;
import com.stayease.housekeeping.enums.RoomStatus;
import com.stayease.housekeeping.exception.InvalidStatusTransitionException;
import com.stayease.housekeeping.repository.RoomRepository;

/**
 * The ONE place that knows the room lifecycle:
 * READY -> DIRTY -> CLEANING -> INSPECTED -> READY. Everything else is rejected.
 */
@Component
public class RoomStatusManager {

    private static final Map<RoomStatus, Set<RoomStatus>> ALLOWED = new EnumMap<>(RoomStatus.class);
    static {
        ALLOWED.put(RoomStatus.READY, EnumSet.of(RoomStatus.DIRTY));
        ALLOWED.put(RoomStatus.DIRTY, EnumSet.of(RoomStatus.CLEANING));
        ALLOWED.put(RoomStatus.CLEANING, EnumSet.of(RoomStatus.INSPECTED));
        ALLOWED.put(RoomStatus.INSPECTED, EnumSet.of(RoomStatus.READY));
    }

    private final RoomRepository roomRepository;
    private final AuditService auditService;

    public RoomStatusManager(RoomRepository roomRepository, AuditService auditService) {
        this.roomRepository = roomRepository;
        this.auditService = auditService;
    }

    /** Throws if the move is not allowed. Nothing is saved before this check. */
    public void validate(Room room, RoomStatus to) {
        if (!ALLOWED.get(room.getStatus()).contains(to)) {
            throw new InvalidStatusTransitionException(
                    "Room " + room.getRoomNumber() + ": cannot change status from " + room.getStatus() + " to " + to + ".");
        }
    }
    public void changeStatus(Room room, RoomStatus to) {
        validate(room, to);
        RoomStatus old = room.getStatus();
        room.setStatus(to);
        roomRepository.save(room);
        auditService.log("ROOM_STATUS_CHANGED", "Room", room.getId(), old.name(), to.name());
    }
}
