package com.api.musiconnect.controller;

import com.api.musiconnect.dto.request.UserAvailabilityRequest;
import com.api.musiconnect.dto.request.UserUpdateRequest;
import com.api.musiconnect.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateUser(
            @PathVariable("id") Long userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        Map<String, String> response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<Map<String, String>> updateAvailability(
            @PathVariable("id") Long userId,
            @Valid @RequestBody UserAvailabilityRequest request
    ) {
        Map<String, String> response = userService.updateAvailability(userId, request);
        return ResponseEntity.ok(response);
    }

}
