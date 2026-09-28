package com.stayease.housekeeping.dto;

public record DashboardSummary(long totalRooms, long dirty, long cleaning, long inspected, long ready,
                               long pendingInspection, long availableHousekeepers, double avgTurnaroundMinutes) { }
