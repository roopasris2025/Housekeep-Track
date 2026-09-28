package com.stayease.housekeeping.controller;


import com.stayease.housekeeping.dto.BookingRequest;
import com.stayease.housekeeping.dto.BookingResponse;
import com.stayease.housekeeping.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import jakarta.validation.Valid;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }

    @GetMapping
    public Page<BookingResponse> list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public BookingResponse update(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        return service.update(id, request);
    }

    // DELETE cancels the booking (status CANCELLED)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
