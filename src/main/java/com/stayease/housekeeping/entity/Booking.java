package com.stayease.housekeeping.entity;

import java.time.LocalDate;

import com.stayease.housekeeping.enums.BookingStatus;

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
import jakarta.persistence.Table;



@Entity
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String guestName;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id")
    private Room room;
    @Column(nullable = false)
    private LocalDate checkIn;
    @Column(nullable = false)
    private LocalDate checkOut;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private BookingStatus status = BookingStatus.BOOKED;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}
