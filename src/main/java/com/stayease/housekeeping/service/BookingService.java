package com.stayease.housekeeping.service;


import com.stayease.housekeeping.dto.BookingRequest;
import com.stayease.housekeeping.dto.BookingResponse;
import com.stayease.housekeeping.entity.Booking;
import com.stayease.housekeeping.entity.Room;
import com.stayease.housekeeping.enums.BookingStatus;
import com.stayease.housekeeping.enums.RoomStatus;
import com.stayease.housekeeping.exception.ConflictException;
import com.stayease.housekeeping.exception.InvalidStatusTransitionException;
import com.stayease.housekeeping.exception.ResourceNotFoundException;
import com.stayease.housekeeping.exception.RoomNotReadyException;
import com.stayease.housekeeping.repository.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.BOOKED, BookingStatus.CHECKED_IN);

    private final BookingRepository bookingRepository;
    private final RoomService roomService;
    private final AuditService auditService;

    public BookingService(BookingRepository bookingRepository, RoomService roomService, AuditService auditService) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> list(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id) {
        return BookingResponse.from(find(id));
    }

    /** RULE 4: a room can be allocated only when its status is READY. */
    @Transactional
    public BookingResponse create(BookingRequest r) {
        Room room = roomService.findRoom(r.roomId());
        checkDates(r.checkIn(), r.checkOut());
        requireReady(room);
        requireNoOverlap(room.getId(), r.checkIn(), r.checkOut(), 0L);

        Booking b = new Booking();
        b.setGuestName(r.guestName());
        b.setRoom(room);
        b.setCheckIn(r.checkIn());
        b.setCheckOut(r.checkOut());
        b.setStatus(BookingStatus.BOOKED);
        bookingRepository.save(b);
        auditService.log("BOOKING_CREATED", "Booking", b.getId(), null, "Room " + room.getRoomNumber() + " for " + b.getGuestName());
        return BookingResponse.from(b);
    }

    @Transactional
    public BookingResponse update(Long id, BookingRequest r) {
        Booking b = find(id);
        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new InvalidStatusTransitionException("A " + b.getStatus() + " booking cannot be edited.");
        }
        checkDates(r.checkIn(), r.checkOut());
        Room room = roomService.findRoom(r.roomId());
        if (!room.getId().equals(b.getRoom().getId())) {          // guest moved to another room
            if (b.getStatus() != BookingStatus.BOOKED) {
                throw new InvalidStatusTransitionException("The room can be changed only while the booking is BOOKED.");
            }
            requireReady(room);
            b.setRoom(room);
        }
        requireNoOverlap(room.getId(), r.checkIn(), r.checkOut(), b.getId());
        b.setGuestName(r.guestName());
        b.setCheckIn(r.checkIn());
        b.setCheckOut(r.checkOut());
        if (r.status() != null && r.status() != b.getStatus()) {
            changeStatus(b, r.status());
        }
        bookingRepository.save(b);
        return BookingResponse.from(b);
    }

    /** DELETE = cancel (history is kept). */
    @Transactional
    public void cancel(Long id) {
        Booking b = find(id);
        if (b.getStatus() != BookingStatus.BOOKED) {
            throw new InvalidStatusTransitionException("Only a BOOKED booking can be cancelled. This one is " + b.getStatus() + ".");
        }
        changeStatus(b, BookingStatus.CANCELLED);
        bookingRepository.save(b);
    }

    private void changeStatus(Booking b, BookingStatus to) {
        Set<BookingStatus> allowed = switch (b.getStatus()) {
            case BOOKED -> Set.of(BookingStatus.CHECKED_IN, BookingStatus.CANCELLED);
            case CHECKED_IN -> Set.of(BookingStatus.CHECKED_OUT);
            default -> Set.of();
        };
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException("Booking cannot change from " + b.getStatus() + " to " + to + ".");
        }
        if (to == BookingStatus.CHECKED_IN) {
            requireReady(b.getRoom());                    // room must still be READY at check-in
        }
        BookingStatus old = b.getStatus();
        b.setStatus(to);
        if (to == BookingStatus.CHECKED_OUT && b.getRoom().getStatus() == RoomStatus.READY) {
            roomService.markDirty(b.getRoom().getId());   // checkout -> room DIRTY -> task auto-created
        }
        auditService.log("BOOKING_STATUS_CHANGED", "Booking", b.getId(), old.name(), to.name());
    }

    private void requireReady(Room room) {
        if (room.getStatus() != RoomStatus.READY) {
            throw new RoomNotReadyException("Room " + room.getRoomNumber() + " is not ready for check-in. Current status: " + room.getStatus() + ".");
        }
    }

    private void checkDates(LocalDate in, LocalDate out) {
        if (!out.isAfter(in)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
    }

    private void requireNoOverlap(Long roomId, LocalDate in, LocalDate out, Long excludeId) {
        if (bookingRepository.countOverlaps(roomId, ACTIVE, in, out, excludeId) > 0) {
            throw new ConflictException("This room is already booked for the selected dates.");
        }
    }

    private Booking find(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + id));
    }
}
