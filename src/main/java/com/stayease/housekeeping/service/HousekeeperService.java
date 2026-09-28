package com.stayease.housekeeping.service;


import com.stayease.housekeeping.dto.HousekeeperRequest;
import com.stayease.housekeeping.dto.HousekeeperResponse;
import com.stayease.housekeeping.entity.Housekeeper;
import com.stayease.housekeeping.enums.HousekeeperStatus;
import com.stayease.housekeeping.enums.TaskStatus;
import com.stayease.housekeeping.exception.ConflictException;
import com.stayease.housekeeping.exception.HousekeeperUnavailableException;
import com.stayease.housekeeping.exception.ResourceNotFoundException;
import com.stayease.housekeeping.repository.CleaningTaskRepository;
import com.stayease.housekeeping.repository.HousekeeperRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
public class HousekeeperService {

    public static final int MAX_ACTIVE_TASKS = 3;   // a housekeeper becomes BUSY at 3 active tasks
    private static final List<TaskStatus> ACTIVE = List.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS);

    private final HousekeeperRepository housekeeperRepository;
    private final CleaningTaskRepository taskRepository;
    private final AuditService auditService;

    public HousekeeperService(HousekeeperRepository housekeeperRepository, CleaningTaskRepository taskRepository,
                              AuditService auditService) {
        this.housekeeperRepository = housekeeperRepository;
        this.taskRepository = taskRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<HousekeeperResponse> list() {
        return housekeeperRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HousekeeperResponse get(Long id) {
        return toResponse(find(id));
    }
    @Transactional
    public HousekeeperResponse create(HousekeeperRequest r) {
        if (housekeeperRepository.existsByEmail(r.email())) {
            throw new ConflictException("A housekeeper with email " + r.email() + " already exists.");
        }
        Housekeeper h = new Housekeeper();
        h.setName(r.name());
        h.setPhone(r.phone());
        h.setEmail(r.email());
        h.setStatus(r.status() == null ? HousekeeperStatus.AVAILABLE : r.status());
        housekeeperRepository.save(h);
        auditService.log("HOUSEKEEPER_CREATED", "Housekeeper", h.getId(), null, h.getName());
        return toResponse(h);
    }
    @Transactional
    public HousekeeperResponse update(Long id, HousekeeperRequest r) {
        Housekeeper h = find(id);
        if (!h.getEmail().equalsIgnoreCase(r.email()) && housekeeperRepository.existsByEmail(r.email())) {
            throw new ConflictException("A housekeeper with email " + r.email() + " already exists.");
        }
        HousekeeperStatus old = h.getStatus();
        h.setName(r.name());
        h.setPhone(r.phone());
        h.setEmail(r.email());
        if (r.status() != null) h.setStatus(r.status());
        housekeeperRepository.save(h);
        refreshStatus(h);   // keeps AVAILABLE/BUSY correct (OFF_DUTY is left alone)
        if (old != h.getStatus()) {
            auditService.log("HOUSEKEEPER_STATUS_CHANGED", "Housekeeper", h.getId(), old.name(), h.getStatus().name());
        }
        return toResponse(h);
    }
    @Transactional
    public void delete(Long id) {
        Housekeeper h = find(id);
        if (taskRepository.existsByHousekeeperId(id)) {
            throw new ConflictException("Housekeeper " + h.getName() + " has cleaning tasks and cannot be deleted. Set OFF_DUTY instead.");
        }
        housekeeperRepository.delete(h);
        auditService.log("HOUSEKEEPER_DELETED", "Housekeeper", id, h.getName(), null);
    }

    // ---------- helpers used by other services ----------

    public Housekeeper find(Long id) {
        return housekeeperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Housekeeper not found with id " + id));
    }

    public long activeCount(Long housekeeperId) {
        return taskRepository.countByHousekeeperIdAndStatusIn(housekeeperId, ACTIVE);
    }

    /** Automatic assignment: AVAILABLE housekeeper with the fewest active tasks. */
    public Housekeeper pickAvailable() {
        return housekeeperRepository.findByStatus(HousekeeperStatus.AVAILABLE).stream()
                .min(Comparator.comparingLong(h -> activeCount(h.getId())))
                .orElseThrow(() -> new HousekeeperUnavailableException("No housekeeper is available right now."));
    }

    /** Manual assignment: the chosen housekeeper must be on duty and below the limit. */
    public Housekeeper getAssignable(Long id) {
        Housekeeper h = find(id);
        if (h.getStatus() == HousekeeperStatus.OFF_DUTY) {
            throw new HousekeeperUnavailableException(h.getName() + " is off duty.");
        }
        if (activeCount(id) >= MAX_ACTIVE_TASKS) {
            throw new HousekeeperUnavailableException(h.getName() + " already has " + MAX_ACTIVE_TASKS + " active tasks.");
        }
        return h;
    }

    /** AVAILABLE if below the limit, BUSY at the limit. OFF_DUTY is never changed automatically. */
    public void refreshStatus(Housekeeper h) {
        if (h.getStatus() == HousekeeperStatus.OFF_DUTY) return;
        h.setStatus(activeCount(h.getId()) >= MAX_ACTIVE_TASKS ? HousekeeperStatus.BUSY : HousekeeperStatus.AVAILABLE);
        housekeeperRepository.save(h);
    }

    private HousekeeperResponse toResponse(Housekeeper h) {
        return HousekeeperResponse.from(h, activeCount(h.getId()),
                taskRepository.countByHousekeeperIdAndStatus(h.getId(), TaskStatus.COMPLETED));
    }
}
