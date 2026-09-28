package com.stayease.housekeeping.controller;


import com.stayease.housekeeping.dto.TaskRequest;
import com.stayease.housekeeping.dto.TaskResponse;
import com.stayease.housekeeping.dto.TaskUpdateRequest;
import com.stayease.housekeeping.service.CleaningTaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/tasks")
public class CleaningTaskController {
    private final CleaningTaskService service;
    public CleaningTaskController(CleaningTaskService service) { this.service = service; }

    // GET /api/tasks?page=0&size=10&sort=assignedAt,desc
    @GetMapping
    public Page<TaskResponse> list(@PageableDefault(size = 10, sort = "assignedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createManual(request));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @RequestBody TaskUpdateRequest request) {
        return service.update(id, request);
    }

    @PutMapping("/{id}/start")
    public TaskResponse start(@PathVariable Long id) { return service.start(id); }

    @PutMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable Long id) { return service.complete(id); }
}
