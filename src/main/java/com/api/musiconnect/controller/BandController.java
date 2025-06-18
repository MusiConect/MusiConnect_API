package com.api.musiconnect.controller;

import com.api.musiconnect.dto.request.AddMemberRequest;
import com.api.musiconnect.dto.request.BandRequest;
import com.api.musiconnect.dto.request.BandUpdateRequest;
import com.api.musiconnect.dto.response.BandResponse;
import com.api.musiconnect.service.BandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
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

    /* NUEVAS FUNCIONALIDADES PARA EL ACRONIMO CRUD */

    // 1. Obtener todas las bandas
    @GetMapping
    public ResponseEntity<List<BandResponse>> listarBandas() {
        return ResponseEntity.ok(bandService.obtenerTodasLasBandas());
    }

    // 2. Obtener una banda por ID
    @GetMapping("/{id}")
    public ResponseEntity<BandResponse> obtenerBandaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bandService.obtenerBandaPorId(id));
    }

    // 3. Eliminar banda
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarBanda(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        return ResponseEntity.ok(bandService.eliminarBanda(id, adminId));
    }

    // 4. Obtener todos los miembros de una banda
    @GetMapping("/{id}/members")
    public ResponseEntity<List<String>> listarMiembros(@PathVariable Long id) {
        return ResponseEntity.ok(bandService.obtenerMiembrosDeBanda(id));
    }

    // 5. Obtener miembro de banda por ID
    @GetMapping("/{id}/members/{miembroId}")
    public ResponseEntity<String> obtenerMiembro(@PathVariable Long id, @PathVariable Long miembroId) {
        return ResponseEntity.ok(bandService.obtenerMiembroDeBandaPorId(id, miembroId));
    }
}
