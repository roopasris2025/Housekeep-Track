package com.stayease.housekeeping.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.stayease.housekeeping.entity.Room;
import com.stayease.housekeeping.enums.RoomStatus;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomNumber(String roomNumber);
    Page<Room> findByStatus(RoomStatus status, Pageable pageable);
    long countByStatus(RoomStatus status);
}
