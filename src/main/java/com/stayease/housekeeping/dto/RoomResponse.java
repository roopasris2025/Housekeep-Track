package com.stayease.housekeeping.dto;

import com.stayease.housekeeping.entity.Room;
import com.stayease.housekeeping.enums.RoomStatus;
import java.time.LocalDateTime;

public record RoomResponse(Long id, String roomNumber, String roomType, Integer floor, RoomStatus status,
                           String assignedHousekeeper, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static RoomResponse from(Room r, String housekeeperName) {
        return new RoomResponse(r.getId(), r.getRoomNumber(), r.getRoomType(), r.getFloor(), r.getStatus(),
                housekeeperName, r.getCreatedAt(), r.getUpdatedAt());
    }
}
