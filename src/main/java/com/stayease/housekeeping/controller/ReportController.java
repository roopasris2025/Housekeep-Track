package com.stayease.housekeeping.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stayease.housekeeping.dto.DashboardSummary;
import com.stayease.housekeeping.dto.HousekeeperResponse;
import com.stayease.housekeeping.dto.TurnaroundResponse;
import com.stayease.housekeeping.service.ReportService;

@RestController
@RequestMapping("/api")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }

    @GetMapping("/reports/housekeeper-workload")
    public List<HousekeeperResponse> workload() { return service.workload(); }

    @GetMapping("/reports/turnaround-time")
    public TurnaroundResponse turnaround() { return service.turnaround(); }

    @GetMapping("/reports/room-status-summary")
    public Map<String, Long> roomStatusSummary() { return service.roomStatusSummary(); }

    @GetMapping("/dashboard/summary")
    public DashboardSummary dashboard() { return service.dashboardSummary(); }
}
