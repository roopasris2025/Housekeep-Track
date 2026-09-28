package com.stayease.housekeeping.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.stayease.housekeeping.enums.InspectionResult;

public record InspectionRequest(
        @NotNull(message = "Task id is required") Long taskId,
        @NotBlank(message = "Supervisor name is required") String supervisorName,
        @NotNull(message = "Result (PASSED or FAILED) is required") InspectionResult result,
        String remarks) { }
