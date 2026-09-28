package com.stayease.housekeeping.dto;

import com.stayease.housekeeping.entity.Inspection;
import com.stayease.housekeeping.enums.InspectionResult;
import java.time.LocalDateTime;

public record InspectionResponse(Long id, Long roomId, String roomNumber, Long taskId, String supervisorName,
                                 InspectionResult result, String remarks, LocalDateTime inspectedAt) {
    public static InspectionResponse from(Inspection i) {
        return new InspectionResponse(i.getId(), i.getRoom().getId(), i.getRoom().getRoomNumber(),
                i.getCleaningTask().getId(), i.getSupervisorName(), i.getResult(), i.getRemarks(), i.getInspectedAt());
    }
}
