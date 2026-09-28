package com.stayease.housekeeping.repository;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stayease.housekeeping.entity.Booking;
import com.stayease.housekeeping.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByRoomId(Long roomId);

    // how many active bookings of this room overlap the requested dates (excludeId = the booking being edited)
    @Query("select count(b) from Booking b where b.room.id = :roomId and b.status in :statuses "
         + "and b.checkIn < :checkOut and b.checkOut > :checkIn and b.id <> :excludeId")
    long countOverlaps(@Param("roomId") Long roomId, @Param("statuses") List<BookingStatus> statuses,
                       @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut,
                       @Param("excludeId") Long excludeId);
}
