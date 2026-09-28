package com.stayease.housekeeping.dto;

import com.stayease.housekeeping.entity.Booking;
import com.stayease.housekeeping.enums.BookingStatus;
import java.time.LocalDate;

public record BookingResponse(Long id, String guestName, Long roomId, String roomNumber,
                              LocalDate checkIn, LocalDate checkOut, BookingStatus status) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getGuestName(), b.getRoom().getId(), b.getRoom().getRoomNumber(),
                b.getCheckIn(), b.getCheckOut(), b.getStatus());
    }
}
