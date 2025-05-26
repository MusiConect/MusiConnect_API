package com.api.musiconnect.controller;

import com.api.musiconnect.dto.request.AddMemberRequest;
import com.api.musiconnect.dto.request.BandRequest;
import com.api.musiconnect.dto.request.BandUpdateRequest;
import com.api.musiconnect.dto.response.BandResponse;
import com.api.musiconnect.service.BandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bands")
@RequiredArgsConstructor
public class BandController {

    private final BandService bandService;

    @PostMapping
    public ResponseEntity<BandResponse> crearBanda(@Valid @RequestBody BandRequest request) {
        BandResponse response = bandService.crearBanda(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/{id}/members") // El {id} es el ID de la banda
    public ResponseEntity<Map<String, String>> addMember(
            @PathVariable("id") Long bandId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        return ResponseEntity.ok(bandService.addIntegrante(bandId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateBand(
            @PathVariable("id") Long bandId,
            @Valid @RequestBody BandUpdateRequest request
    ) {
        return ResponseEntity.ok(bandService.updateBand(bandId, request));
    } 
}
