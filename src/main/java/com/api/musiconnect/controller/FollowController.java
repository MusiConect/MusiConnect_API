package com.api.musiconnect.controller;

import com.api.musiconnect.dto.request.FollowRequest;
import com.api.musiconnect.dto.request.UnfollowRequest;
import com.api.musiconnect.dto.response.FollowResponse;
import com.api.musiconnect.dto.response.FollowedProfileResponse;
import com.api.musiconnect.service.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<FollowResponse> crearFollow(@Valid @RequestBody FollowRequest request) {
        FollowResponse response = followService.crearFollow(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<FollowedProfileResponse>> listarPerfilesSeguidos(@PathVariable Long userId) {
        List<FollowedProfileResponse> response = followService.listarPerfilesSeguidos(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> eliminarFollow(
            @Valid @RequestBody UnfollowRequest request
    ) {
        Map<String, String> response = followService.eliminarFollow(request);
        return ResponseEntity.ok(response);
    }


}
