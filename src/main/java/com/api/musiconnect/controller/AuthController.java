package com.api.musiconnect.controller;

import com.api.musiconnect.dto.request.LoginRequest;
import com.api.musiconnect.dto.request.UserRequest;
import com.api.musiconnect.dto.response.LoginResponse;
import com.api.musiconnect.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
