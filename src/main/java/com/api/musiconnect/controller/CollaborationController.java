package com.api.musiconnect.controller;

import com.api.musiconnect.dto.request.CollaborationRequest;
import com.api.musiconnect.dto.request.CollaborationUpdateRequest;
import com.api.musiconnect.dto.response.CollaborationResponse;
import com.api.musiconnect.service.CollaborationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collaborations")
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;

    @PostMapping
    public ResponseEntity<CollaborationResponse> crearColaboracion(
            @Valid @RequestBody CollaborationRequest request) {
        CollaborationResponse response = collaborationService.crearColaboracion(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}") // {id} es el ID de la colaboración que se va a actualizar
    public ResponseEntity<Map<String, String>> updateCollaboration(
            @PathVariable("id") Long collaborationId,
            @Valid @RequestBody CollaborationUpdateRequest request
    ) {
        return ResponseEntity.ok(collaborationService.updateCollaboration(collaborationId, request));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CollaborationResponse>> listarColaboracionesActivas() {
        List<CollaborationResponse> activas = collaborationService.listarColaboracionesActivas();
        return ResponseEntity.ok(activas);
    }

    @GetMapping
    public ResponseEntity<List<CollaborationResponse>> getAll() {
        return ResponseEntity.ok(collaborationService.getAllCollaborations());
    }

    @GetMapping("/usuario/{nombreArtistico}")
    public ResponseEntity<List<CollaborationResponse>> getByNombreArtistico(@PathVariable String nombreArtistico) {
        return ResponseEntity.ok(collaborationService.getByNombreArtistico(nombreArtistico));
    }

    @PatchMapping("/{id}/add-member")
    public ResponseEntity<Map<String, String>> addColaborador(
            @PathVariable Long id,
            @RequestParam String nombreArtistico) {
        return ResponseEntity.ok(collaborationService.addColaborador(id, nombreArtistico));
    }

    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteColaboration(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(collaborationService.deleteCollaboration(id, userId));
    }
}
