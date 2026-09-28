package com.stayease.housekeeping.controller;


import com.stayease.housekeeping.dto.InspectionRequest;
import com.stayease.housekeeping.dto.InspectionResponse;
import com.stayease.housekeeping.dto.TaskResponse;
import com.stayease.housekeeping.service.CleaningTaskService;
import com.stayease.housekeeping.service.InspectionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {
    private final InspectionService inspectionService;
    private final CleaningTaskService taskService;

    public InspectionController(InspectionService inspectionService, CleaningTaskService taskService) {
        this.inspectionService = inspectionService;
        this.taskService = taskService;
    }

    @GetMapping
    public Page<InspectionResponse> list(@PageableDefault(size = 10, sort = "inspectedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return inspectionService.list(pageable);
    }

    // completed cleaning tasks that are waiting for the supervisor
    @GetMapping("/pending")
    public List<TaskResponse> pending() { return taskService.pendingInspection(); }

    @GetMapping("/{id}")
    public InspectionResponse get(@PathVariable Long id) { return inspectionService.get(id); }

    @PostMapping
    public ResponseEntity<InspectionResponse> create(@Valid @RequestBody InspectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inspectionService.create(request));
    }
}
