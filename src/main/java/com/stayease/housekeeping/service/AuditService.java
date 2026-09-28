package com.stayease.housekeeping.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.stayease.housekeeping.entity.AuditLog;
import com.stayease.housekeeping.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

/** Saves one row in audit_logs for every important change. */
@Service
public class AuditService {
    private final AuditLogRepository repository;
    private final HttpServletRequest request;   // gives us the X-Performed-By header

    public AuditService(AuditLogRepository repository, HttpServletRequest request) {
        this.repository = repository;
        this.request = request;
    }

    public void log(String action, String entityName, Long entityId, String oldValue, String newValue) {
        AuditLog a = new AuditLog();
        a.setAction(action);
        a.setEntityName(entityName);
        a.setEntityId(entityId);
        a.setOldValue(oldValue);
        a.setNewValue(newValue);
        a.setPerformedBy(currentUser());
        repository.save(a);
    }

    public Page<AuditLog> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    private String currentUser() {
        try {
            if (request == null) {
                return "System";
            }
            String user = request.getHeader("X-Performed-By");
            return (user == null || user.isBlank()) ? "System" : user.trim();
        } catch (Exception e) {
            return "System";   // no web request (e.g. data seeder)
        }
    }
}
