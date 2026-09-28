package com.stayease.housekeeping.entity;

import java.time.LocalDateTime;

import com.stayease.housekeeping.enums.InspectionResult;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;



@Entity
@Table(name = "inspections")
public class Inspection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id")
    private Room room;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cleaning_task_id", unique = true)
    private CleaningTask cleaningTask;
    @Column(nullable = false)
    private String supervisorName;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private InspectionResult result;
    @Column(length = 500)
    private String remarks;
    private LocalDateTime inspectedAt;

    @PrePersist
    @SuppressWarnings("unused")
    private void onCreate() { inspectedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public CleaningTask getCleaningTask() { return cleaningTask; }
    public void setCleaningTask(CleaningTask cleaningTask) { this.cleaningTask = cleaningTask; }
    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }
    public InspectionResult getResult() { return result; }
    public void setResult(InspectionResult result) { this.result = result; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(LocalDateTime inspectedAt) { this.inspectedAt = inspectedAt; }
}
