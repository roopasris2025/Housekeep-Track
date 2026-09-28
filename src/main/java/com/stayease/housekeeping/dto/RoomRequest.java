package com.stayease.housekeeping.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.stayease.housekeeping.enums.RoomStatus;

public record RoomRequest(
        @NotBlank(message = "Room number is required") String roomNumber,
        @NotBlank(message = "Room type is required") String roomType,
        @NotNull(message = "Floor is required") @Positive(message = "Floor must be a positive number") Integer floor,
        RoomStatus status) { }
