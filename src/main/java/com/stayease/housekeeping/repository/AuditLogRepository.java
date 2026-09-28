package com.stayease.housekeeping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stayease.housekeeping.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { }
