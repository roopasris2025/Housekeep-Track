package com.stayease.housekeeping.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stayease.housekeeping.entity.Housekeeper;
import com.stayease.housekeeping.enums.HousekeeperStatus;

public interface HousekeeperRepository extends JpaRepository<Housekeeper, Long> {
    List<Housekeeper> findByStatus(HousekeeperStatus status);
    boolean existsByEmail(String email);
    long countByStatus(HousekeeperStatus status);
}
