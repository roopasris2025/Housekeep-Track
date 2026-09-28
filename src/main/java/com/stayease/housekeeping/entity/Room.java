package com.stayease.housekeeping.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.stayease.housekeeping.enums.RoomStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;



@Entity
@Table(name = "rooms")
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String roomNumber;
    @Column(nullable = false)
    private String roomType;
    @Column(nullable = false)
    private Integer floor;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RoomStatus status = RoomStatus.READY;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room")
    private List<CleaningTask> tasks = new ArrayList<>();
    @OneToMany(mappedBy = "room")
    private List<Inspection> inspections = new ArrayList<>();
    @OneToMany(mappedBy = "room")
    private List<Booking> bookings = new ArrayList<>();

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }

    @PreUpdate
    @SuppressWarnings("unused")
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<CleaningTask> getTasks() { return tasks; }
    public void setTasks(List<CleaningTask> tasks) { this.tasks = tasks; }
    public List<Inspection> getInspections() { return inspections; }
    public void setInspections(List<Inspection> inspections) { this.inspections = inspections; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}
