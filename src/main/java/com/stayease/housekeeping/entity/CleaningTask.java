package com.stayease.housekeeping.entity;

import java.time.LocalDateTime;

import com.stayease.housekeeping.enums.TaskStatus;

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
import jakarta.persistence.Table;



@Entity
@Table(name = "cleaning_tasks")
public class CleaningTask {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "housekeeper_id")
    private Housekeeper housekeeper;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TaskStatus status = TaskStatus.ASSIGNED;
    private LocalDateTime dirtyAt;      // when the room became DIRTY (used for turnaround time)
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    @Column(length = 500)
    private String notes;

    @OneToOne(mappedBy = "cleaningTask")
    private Inspection inspection;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Housekeeper getHousekeeper() { return housekeeper; }
    public void setHousekeeper(Housekeeper housekeeper) { this.housekeeper = housekeeper; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public LocalDateTime getDirtyAt() { return dirtyAt; }
    public void setDirtyAt(LocalDateTime dirtyAt) { this.dirtyAt = dirtyAt; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Inspection getInspection() { return inspection; }
    public void setInspection(Inspection inspection) { this.inspection = inspection; }
}
