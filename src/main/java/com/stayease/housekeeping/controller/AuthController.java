package com.stayease.housekeeping.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stayease.housekeeping.dto.LoginRequest;
import com.stayease.housekeeping.dto.LoginResponse;
import com.stayease.housekeeping.exception.InvalidCredentialsException;

import jakarta.validation.Valid;

/** Very simple demo login: users are read from application.properties (no Spring Security / JWT). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${stayease.users}")
    private String users;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        for (String entry : users.split(";")) {
            String[] p = entry.trim().split(":");          // email : password : name : role
            if (p.length == 4 && p[0].equalsIgnoreCase(request.email()) && p[1].equals(request.password())) {
                return new LoginResponse(p[2], p[0], p[3]);
            }
        }
        throw new InvalidCredentialsException("Invalid email or password.");
    }
}
