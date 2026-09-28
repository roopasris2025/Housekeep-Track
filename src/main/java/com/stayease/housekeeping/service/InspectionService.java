package com.stayease.housekeeping.service;


import com.stayease.housekeeping.dto.InspectionRequest;
import com.stayease.housekeeping.dto.InspectionResponse;
import com.stayease.housekeeping.entity.CleaningTask;
import com.stayease.housekeeping.entity.Inspection;
import com.stayease.housekeeping.entity.Room;
import com.stayease.housekeeping.enums.InspectionResult;
import com.stayease.housekeeping.enums.RoomStatus;
import com.stayease.housekeeping.enums.TaskStatus;
import com.stayease.housekeeping.exception.ConflictException;
import com.stayease.housekeeping.exception.InvalidStatusTransitionException;
import com.stayease.housekeeping.exception.ResourceNotFoundException;
import com.stayease.housekeeping.repository.InspectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final CleaningTaskService taskService;
    private final RoomStatusManager statusManager;
    private final AuditService auditService;

    public InspectionService(InspectionRepository inspectionRepository, CleaningTaskService taskService,
                             RoomStatusManager statusManager, AuditService auditService) {
        this.inspectionRepository = inspectionRepository;
        this.taskService = taskService;
        this.statusManager = statusManager;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<InspectionResponse> list(Pageable pageable) {
        return inspectionRepository.findAll(pageable).map(InspectionResponse::from);
    }

    @Transactional(readOnly = true)
    public InspectionResponse get(Long id) {
        return inspectionRepository.findById(id).map(InspectionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found with id " + id));
    }

    /** RULE 3 */
    @Transactional
    public InspectionResponse create(InspectionRequest r) {
        CleaningTask task = taskService.find(r.taskId());
        Room room = task.getRoom();

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new InvalidStatusTransitionException("Room " + room.getRoomNumber() + " cannot be inspected yet. Cleaning task #"
                    + task.getId() + " is " + task.getStatus() + ", it must be COMPLETED.");
        }
        if (inspectionRepository.existsByCleaningTaskId(task.getId())) {
            throw new ConflictException("Cleaning task #" + task.getId() + " has already been inspected.");
        }
        boolean failed = r.result() == InspectionResult.FAILED;
        if (failed && (r.remarks() == null || r.remarks().isBlank())) {
            throw new IllegalArgumentException("Remarks are required when an inspection fails.");
        }

        Inspection i = new Inspection();
        i.setRoom(room);
        i.setCleaningTask(task);
        i.setSupervisorName(r.supervisorName());
        i.setResult(r.result());
        i.setRemarks(r.remarks());
        inspectionRepository.save(i);

        if (failed) {
            // room stays CLEANING, a new task is created and assigned for re-cleaning
            taskService.createAndAssign(room, task.getDirtyAt(), "Re-clean after failed inspection: " + r.remarks());
            auditService.log("INSPECTION_FAILED", "Room", room.getId(), "CLEANING", "CLEANING (re-clean): " + r.remarks());
        } else {
            statusManager.changeStatus(room, RoomStatus.INSPECTED);   // CLEANING -> INSPECTED
            statusManager.changeStatus(room, RoomStatus.READY);       // INSPECTED -> READY
            auditService.log("INSPECTION_PASSED", "Room", room.getId(), "CLEANING", "READY");
        }
        return InspectionResponse.from(i);
    }
}
