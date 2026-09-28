package com.stayease.housekeeping.service;


import com.stayease.housekeeping.dto.TaskRequest;
import com.stayease.housekeeping.dto.TaskResponse;
import com.stayease.housekeeping.dto.TaskUpdateRequest;
import com.stayease.housekeeping.entity.CleaningTask;
import com.stayease.housekeeping.entity.Housekeeper;
import com.stayease.housekeeping.entity.Room;
import com.stayease.housekeeping.enums.RoomStatus;
import com.stayease.housekeeping.enums.TaskStatus;
import com.stayease.housekeeping.exception.ConflictException;
import com.stayease.housekeeping.exception.InvalidStatusTransitionException;
import com.stayease.housekeeping.exception.ResourceNotFoundException;
import com.stayease.housekeeping.repository.CleaningTaskRepository;
import com.stayease.housekeeping.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CleaningTaskService {

    private static final List<TaskStatus> ACTIVE = List.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS);

    private final CleaningTaskRepository taskRepository;
    private final RoomRepository roomRepository;
    private final HousekeeperService housekeeperService;
    private final RoomStatusManager statusManager;
    private final AuditService auditService;

    public CleaningTaskService(CleaningTaskRepository taskRepository, RoomRepository roomRepository,
                               HousekeeperService housekeeperService, RoomStatusManager statusManager,
                               AuditService auditService) {
        this.taskRepository = taskRepository;
        this.roomRepository = roomRepository;
        this.housekeeperService = housekeeperService;
        this.statusManager = statusManager;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(Pageable pageable) {
        return taskRepository.findAll(pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        return TaskResponse.from(find(id));
    }

    /** Completed tasks that are waiting for the supervisor. */
    @Transactional(readOnly = true)
    public List<TaskResponse> pendingInspection() {
        return taskRepository.findPendingInspection().stream().map(TaskResponse::from).toList();
    }

    /** RULE 1: called automatically when a room becomes DIRTY (or after a failed inspection). */
    @Transactional
    public CleaningTask createAndAssign(Room room, LocalDateTime dirtyAt, String notes) {
        Housekeeper hk = housekeeperService.pickAvailable();   // throws if nobody is free
        return saveNewTask(room, hk, dirtyAt, notes);
    }

    /** Manual fallback: POST /api/tasks */
    @Transactional
    public TaskResponse createManual(TaskRequest r) {
        Room room = roomRepository.findById(r.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + r.roomId()));
        if (room.getStatus() != RoomStatus.DIRTY && room.getStatus() != RoomStatus.CLEANING) {
            throw new InvalidStatusTransitionException("Room " + room.getRoomNumber() + " is " + room.getStatus()
                    + ". A cleaning task can only be created for a DIRTY or CLEANING room.");
        }
        if (taskRepository.existsByRoomIdAndStatusIn(room.getId(), ACTIVE)) {
            throw new ConflictException("Room " + room.getRoomNumber() + " already has an active cleaning task.");
        }
        Housekeeper hk = (r.housekeeperId() == null)
                ? housekeeperService.pickAvailable()
                : housekeeperService.getAssignable(r.housekeeperId());
        return TaskResponse.from(saveNewTask(room, hk, LocalDateTime.now(), r.notes()));
    }

    /** Edit notes or re-assign - only while the task is still ASSIGNED. */
    @Transactional
    public TaskResponse update(Long id, TaskUpdateRequest r) {
        CleaningTask t = find(id);
        if (t.getStatus() != TaskStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Only an ASSIGNED task can be edited. This task is " + t.getStatus() + ".");
        }
        if (r.notes() != null) t.setNotes(r.notes());
        if (r.housekeeperId() != null && !r.housekeeperId().equals(t.getHousekeeper().getId())) {
            Housekeeper oldHk = t.getHousekeeper();
            Housekeeper newHk = housekeeperService.getAssignable(r.housekeeperId());
            t.setHousekeeper(newHk);
            taskRepository.save(t);
            housekeeperService.refreshStatus(oldHk);
            housekeeperService.refreshStatus(newHk);
            auditService.log("TASK_REASSIGNED", "CleaningTask", t.getId(), oldHk.getName(), newHk.getName());
        }
        taskRepository.save(t);
        return TaskResponse.from(t);
    }

    /** ASSIGNED -> IN_PROGRESS, and the room DIRTY -> CLEANING. */
    @Transactional
    public TaskResponse start(Long id) {
        CleaningTask t = find(id);
        if (t.getStatus() != TaskStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Cleaning can start only from ASSIGNED. This task is " + t.getStatus() + ".");
        }
        t.setStatus(TaskStatus.IN_PROGRESS);
        t.setStartedAt(LocalDateTime.now());
        taskRepository.save(t);
        Room room = t.getRoom();
        if (room.getStatus() == RoomStatus.DIRTY) {          // after a failed inspection the room is already CLEANING
            statusManager.changeStatus(room, RoomStatus.CLEANING);
        }
        auditService.log("TASK_STARTED", "CleaningTask", t.getId(), "ASSIGNED", "IN_PROGRESS");
        return TaskResponse.from(t);
    }

    /** IN_PROGRESS -> COMPLETED. The room stays CLEANING until the supervisor inspects it. */
    @Transactional
    public TaskResponse complete(Long id) {
        CleaningTask t = find(id);
        if (t.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new InvalidStatusTransitionException("Only an IN_PROGRESS task can be completed. This task is " + t.getStatus() + ".");
        }
        t.setStatus(TaskStatus.COMPLETED);
        t.setCompletedAt(LocalDateTime.now());
        taskRepository.save(t);
        housekeeperService.refreshStatus(t.getHousekeeper());   // frees the housekeeper if they were BUSY
        auditService.log("TASK_COMPLETED", "CleaningTask", t.getId(), "IN_PROGRESS", "COMPLETED");
        return TaskResponse.from(t);
    }

    public CleaningTask find(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cleaning task not found with id " + id));
    }

    private CleaningTask saveNewTask(Room room, Housekeeper hk, LocalDateTime dirtyAt, String notes) {
        CleaningTask t = new CleaningTask();
        t.setRoom(room);
        t.setHousekeeper(hk);
        t.setStatus(TaskStatus.ASSIGNED);
        t.setDirtyAt(dirtyAt);
        t.setAssignedAt(LocalDateTime.now());
        t.setNotes(notes);
        taskRepository.save(t);
        housekeeperService.refreshStatus(hk);
        auditService.log("TASK_ASSIGNED", "CleaningTask", t.getId(), null,
                "Room " + room.getRoomNumber() + " -> " + hk.getName());
        return t;
    }
}
