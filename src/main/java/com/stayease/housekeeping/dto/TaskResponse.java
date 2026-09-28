package com.stayease.housekeeping.dto;

import com.stayease.housekeeping.entity.CleaningTask;
import com.stayease.housekeeping.enums.TaskStatus;
import java.time.LocalDateTime;

public record TaskResponse(Long id, Long roomId, String roomNumber, Long housekeeperId, String housekeeperName,
                           TaskStatus status, LocalDateTime dirtyAt, LocalDateTime assignedAt, LocalDateTime startedAt,
                           LocalDateTime completedAt, String notes, boolean inspected) {
    public static TaskResponse from(CleaningTask t) {
        return new TaskResponse(t.getId(), t.getRoom().getId(), t.getRoom().getRoomNumber(),
                t.getHousekeeper().getId(), t.getHousekeeper().getName(), t.getStatus(), t.getDirtyAt(),
                t.getAssignedAt(), t.getStartedAt(), t.getCompletedAt(), t.getNotes(), t.getInspection() != null);
    }
}
