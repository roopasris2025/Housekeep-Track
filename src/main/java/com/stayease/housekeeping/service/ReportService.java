package com.stayease.housekeeping.service;


import com.stayease.housekeeping.dto.DashboardSummary;
import com.stayease.housekeeping.dto.HousekeeperResponse;
import com.stayease.housekeeping.dto.TurnaroundResponse;
import com.stayease.housekeeping.entity.Inspection;
import com.stayease.housekeeping.enums.HousekeeperStatus;
import com.stayease.housekeeping.enums.InspectionResult;
import com.stayease.housekeeping.enums.RoomStatus;
import com.stayease.housekeeping.repository.CleaningTaskRepository;
import com.stayease.housekeeping.repository.HousekeeperRepository;
import com.stayease.housekeeping.repository.InspectionRepository;
import com.stayease.housekeeping.repository.RoomRepository;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final RoomRepository roomRepository;
    private final HousekeeperRepository housekeeperRepository;
    private final CleaningTaskRepository taskRepository;
    private final InspectionRepository inspectionRepository;
    private final HousekeeperService housekeeperService;

    public ReportService(RoomRepository roomRepository, HousekeeperRepository housekeeperRepository,
                         CleaningTaskRepository taskRepository, InspectionRepository inspectionRepository,
                         HousekeeperService housekeeperService) {
        this.roomRepository = roomRepository;
        this.housekeeperRepository = housekeeperRepository;
        this.taskRepository = taskRepository;
        this.inspectionRepository = inspectionRepository;
        this.housekeeperService = housekeeperService;
    }

    /** Name, status, active tasks, completed tasks for every housekeeper. */
    public List<HousekeeperResponse> workload() {
        return housekeeperService.list();
    }

    /** Average of (inspection passed time - room dirty time), in minutes. */
    @Transactional(readOnly = true)
    public TurnaroundResponse turnaround() {
        long totalSeconds = 0;
        int count = 0;
        for (Inspection i : inspectionRepository.findByResult(InspectionResult.PASSED)) {
            LocalDateTime dirtyAt = i.getCleaningTask().getDirtyAt();
            if (dirtyAt == null) continue;
            totalSeconds += Duration.between(dirtyAt, i.getInspectedAt()).getSeconds();
            count++;
        }
        double avg = (count == 0) ? 0 : Math.round(totalSeconds / 60.0 / count * 10) / 10.0;
        return new TurnaroundResponse(avg, count);
    }

    public Map<String, Long> roomStatusSummary() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (RoomStatus s : RoomStatus.values()) {
            map.put(s.name(), roomRepository.countByStatus(s));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public DashboardSummary dashboardSummary() {
        return new DashboardSummary(
                roomRepository.count(),
                roomRepository.countByStatus(RoomStatus.DIRTY),
                roomRepository.countByStatus(RoomStatus.CLEANING),
                roomRepository.countByStatus(RoomStatus.INSPECTED),
                roomRepository.countByStatus(RoomStatus.READY),
                taskRepository.countPendingInspection(),
                housekeeperRepository.countByStatus(HousekeeperStatus.AVAILABLE),
                turnaround().averageMinutes());
    }
}
