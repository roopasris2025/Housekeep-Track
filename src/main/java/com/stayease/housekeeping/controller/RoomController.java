package com.stayease.housekeeping.controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stayease.housekeeping.dto.RoomRequest;
import com.stayease.housekeeping.dto.RoomResponse;
import com.stayease.housekeeping.enums.RoomStatus;
import com.stayease.housekeeping.service.RoomService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;
    public RoomController(RoomService roomService) { this.roomService = roomService; }

    // GET /api/rooms?status=READY&page=0&size=10&sort=roomNumber,asc
    @GetMapping
    public Page<RoomResponse> list(@RequestParam(required = false) RoomStatus status, Pageable pageable) {
        return roomService.list(status, pageable);
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable Long id) { return roomService.get(id); }

    @PostMapping
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(request));
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // marks the room DIRTY -> creates and assigns a cleaning task automatically
    @PostMapping("/{id}/dirty")
    public RoomResponse markDirty(@PathVariable Long id) { return roomService.markDirty(id); }
}
