package com.stayease.housekeeping.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stayease.housekeeping.entity.Inspection;
import com.stayease.housekeeping.enums.InspectionResult;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    boolean existsByRoomId(Long roomId);
    boolean existsByCleaningTaskId(Long taskId);
    List<Inspection> findByResult(InspectionResult result);
}
