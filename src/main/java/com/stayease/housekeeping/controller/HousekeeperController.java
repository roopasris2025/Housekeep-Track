package com.stayease.housekeeping.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stayease.housekeeping.dto.HousekeeperRequest;
import com.stayease.housekeeping.dto.HousekeeperResponse;
import com.stayease.housekeeping.service.HousekeeperService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/housekeepers")
public class HousekeeperController {
    private final HousekeeperService service;
    public HousekeeperController(HousekeeperService service) { this.service = service; }

    @GetMapping
    public List<HousekeeperResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    public HousekeeperResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public ResponseEntity<HousekeeperResponse> create(@Valid @RequestBody HousekeeperRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public HousekeeperResponse update(@PathVariable Long id, @Valid @RequestBody HousekeeperRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
