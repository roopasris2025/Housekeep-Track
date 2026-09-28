package com.stayease.housekeeping.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.stayease.housekeeping.enums.BookingStatus;
import java.time.LocalDate;

public record BookingRequest(
        @NotBlank(message = "Guest name is required") String guestName,
        @NotNull(message = "Room id is required") Long roomId,
        @NotNull(message = "Check-in date is required") LocalDate checkIn,
        @NotNull(message = "Check-out date is required") LocalDate checkOut,
        BookingStatus status) { }
