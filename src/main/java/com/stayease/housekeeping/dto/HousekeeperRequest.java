package com.stayease.housekeeping.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.stayease.housekeeping.enums.HousekeeperStatus;

public record HousekeeperRequest(
        @NotBlank(message = "Name is required") String name,
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits") String phone,
        @NotBlank(message = "Email is required") @Email(message = "Email format is invalid") String email,
        HousekeeperStatus status) { }
