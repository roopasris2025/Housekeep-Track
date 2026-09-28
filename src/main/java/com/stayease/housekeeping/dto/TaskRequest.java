package com.stayease.housekeeping.dto;

import jakarta.validation.constraints.NotNull;

public record TaskRequest(
        @NotNull(message = "Room id is required") Long roomId,
        Long housekeeperId,      // optional: if empty, the system picks one
        String notes) { }
