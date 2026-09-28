package com.stayease.housekeeping.dto;

import com.stayease.housekeeping.entity.Housekeeper;
import com.stayease.housekeeping.enums.HousekeeperStatus;

public record HousekeeperResponse(Long id, String name, String phone, String email, HousekeeperStatus status,
                                  long activeTasks, long completedTasks) {
    public static HousekeeperResponse from(Housekeeper h, long active, long completed) {
        return new HousekeeperResponse(h.getId(), h.getName(), h.getPhone(), h.getEmail(), h.getStatus(), active, completed);
    }
}
