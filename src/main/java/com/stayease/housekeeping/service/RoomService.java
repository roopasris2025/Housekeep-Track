package com.stayease.housekeeping.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stayease.housekeeping.dto.RoomRequest;
import com.stayease.housekeeping.dto.RoomResponse;
import com.stayease.housekeeping.entity.Room;
import com.stayease.housekeeping.enums.RoomStatus;
import com.stayease.housekeeping.exception.ConflictException;
import com.stayease.housekeeping.exception.InvalidStatusTransitionException;
import com.stayease.housekeeping.exception.ResourceNotFoundException;
import com.stayease.housekeeping.repository.BookingRepository;
import com.stayease.housekeeping.repository.CleaningTaskRepository;
import com.stayease.housekeeping.repository.InspectionRepository;
import com.stayease.housekeeping.repository.RoomRepository;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final CleaningTaskRepository taskRepository;
    private final InspectionRepository inspectionRepository;
    private final BookingRepository bookingRepository;
    private final RoomStatusManager statusManager;
    private final CleaningTaskService taskService;
    private final AuditService auditService;

    public RoomService(RoomRepository roomRepository, CleaningTaskRepository taskRepository,
                       InspectionRepository inspectionRepository, BookingRepository bookingRepository,
                       RoomStatusManager statusManager, CleaningTaskService taskService, AuditService auditService) {
        this.roomRepository = roomRepository;
        this.taskRepository = taskRepository;
        this.inspectionRepository = inspectionRepository;
        this.bookingRepository = bookingRepository;
        this.statusManager = statusManager;
        this.taskService = taskService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<RoomResponse> list(RoomStatus status, Pageable pageable) {
        Page<Room> page = (status == null) ? roomRepository.findAll(pageable) : roomRepository.findByStatus(status, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RoomResponse get(Long id) {
        return toResponse(findRoom(id));
    }
    @Transactional
    public RoomResponse create(RoomRequest r) {
        if (roomRepository.existsByRoomNumber(r.roomNumber())) {
            throw new ConflictException("Room " + r.roomNumber() + " already exists.");
        }
        Room room = new Room();
        room.setRoomNumber(r.roomNumber());
        room.setRoomType(r.roomType());
        room.setFloor(r.floor());
        room.setStatus(RoomStatus.READY);   // a new room starts READY
        roomRepository.save(room);
        auditService.log("ROOM_CREATED", "Room", room.getId(), null, room.getRoomNumber());
        return toResponse(room);
    }
    @Transactional
    public RoomResponse update(Long id, RoomRequest r) {
        Room room = findRoom(id);
        if (!room.getRoomNumber().equals(r.roomNumber()) && roomRepository.existsByRoomNumber(r.roomNumber())) {
            throw new ConflictException("Room " + r.roomNumber() + " already exists.");
        }
        room.setRoomNumber(r.roomNumber());
        room.setRoomType(r.roomType());
        room.setFloor(r.floor());

        if (r.status() != null && r.status() != room.getStatus()) {
            if (r.status() == RoomStatus.DIRTY) {
                doMarkDirty(room);                       // full workflow: task + housekeeper
            } else {
                statusManager.validate(room, r.status());   // e.g. DIRTY -> READY is rejected here
                throw new InvalidStatusTransitionException("Room status " + r.status()
                        + " can only be reached through the cleaning task and inspection workflow.");
            }
        }
        roomRepository.save(room);
        return toResponse(room);
    }
    @Transactional
    public void delete(Long id) {
        Room room = findRoom(id);
        if (taskRepository.existsByRoomId(id) || inspectionRepository.existsByRoomId(id) || bookingRepository.existsByRoomId(id)) {
            throw new ConflictException("Room " + room.getRoomNumber() + " has tasks, inspections or bookings and cannot be deleted.");
        }
        roomRepository.delete(room);
        auditService.log("ROOM_DELETED", "Room", id, room.getRoomNumber(), null);
    }

    /** POST /api/rooms/{id}/dirty : update status + create task + assign housekeeper (all or nothing). */
    @Transactional
    public RoomResponse markDirty(Long id) {
        return toResponse(doMarkDirty(findRoom(id)));
    }

    private Room doMarkDirty(Room room) {
        statusManager.changeStatus(room, RoomStatus.DIRTY);                     // 1. validates + updates status
        taskService.createAndAssign(room, LocalDateTime.now(), null);           // 2-4. finds housekeeper, creates + assigns task
        return room;                                                            // if no housekeeper: exception -> rollback
    }

    public Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));
    }

    private RoomResponse toResponse(Room room) {
        String housekeeper = null;
        if (room.getStatus() == RoomStatus.DIRTY || room.getStatus() == RoomStatus.CLEANING) {
            housekeeper = taskRepository.findFirstByRoomIdOrderByIdDesc(room.getId())
                    .map(t -> t.getHousekeeper().getName()).orElse(null);
        }
        return RoomResponse.from(room, housekeeper);
    }
}
