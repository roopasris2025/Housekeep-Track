package com.stayease.housekeeping.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.stayease.housekeeping.entity.CleaningTask;
import com.stayease.housekeeping.enums.TaskStatus;

public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {
    long countByHousekeeperIdAndStatusIn(Long housekeeperId, List<TaskStatus> statuses);
    long countByHousekeeperIdAndStatus(Long housekeeperId, TaskStatus status);
    boolean existsByRoomId(Long roomId);
    boolean existsByHousekeeperId(Long housekeeperId);
    boolean existsByRoomIdAndStatusIn(Long roomId, List<TaskStatus> statuses);
    Optional<CleaningTask> findFirstByRoomIdOrderByIdDesc(Long roomId);

    // COMPLETED tasks that do not have an inspection yet = "pending inspection"
    @Query("select t from CleaningTask t where t.status = com.stayease.housekeeping.enums.TaskStatus.COMPLETED "
         + "and not exists (select i from Inspection i where i.cleaningTask = t) order by t.completedAt")
    List<CleaningTask> findPendingInspection();

    @Query("select count(t) from CleaningTask t where t.status = com.stayease.housekeeping.enums.TaskStatus.COMPLETED "
         + "and not exists (select i from Inspection i where i.cleaningTask = t)")
    long countPendingInspection();
}
