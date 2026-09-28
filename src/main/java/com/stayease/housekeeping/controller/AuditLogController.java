package com.stayease.housekeeping.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stayease.housekeeping.entity.AuditLog;
import com.stayease.housekeeping.service.AuditService;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditService auditService;
    public AuditLogController(AuditService auditService) { this.auditService = auditService; }

    @GetMapping
    public Page<AuditLog> list(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return auditService.list(pageable);
    }
}
