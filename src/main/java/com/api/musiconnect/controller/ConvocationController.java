package com.api.musiconnect.controller;

import com.api.musiconnect.dto.request.ConvocationRequest;
import com.api.musiconnect.dto.request.ConvocationUpdateRequest;
import com.api.musiconnect.dto.request.FavoriteConvocationRequest;
import com.api.musiconnect.dto.response.ConvocationResponse;
import com.api.musiconnect.service.ConvocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/convocations")
@RequiredArgsConstructor
public class ConvocationController {

    private final ConvocationService convocationService;

    @PostMapping
    public ResponseEntity<ConvocationResponse> crearConvocatoria(@Valid @RequestBody ConvocationRequest request) {
        ConvocationResponse response = convocationService.crearConvocatoria(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> editarConvocatoria(
            @PathVariable Long id,
            @Valid @RequestBody ConvocationUpdateRequest request
    ) {
        Map<String, String> response = convocationService.editarConvocatoria(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ConvocationResponse>> listarConvocatoriasActivas() {
        List<ConvocationResponse> response = convocationService.listarConvocatoriasActivas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/favorites")
    public ResponseEntity<List<ConvocationResponse>> listarFavoritasPorUsuario(@PathVariable Long userId) {
        List<ConvocationResponse> response = convocationService.listarConvocatoriasFavoritasPorUsuario(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/favorites")
    public ResponseEntity<Map<String, String>> marcarComoFavorita(@Valid @RequestBody FavoriteConvocationRequest request) {
        Map<String, String> response = convocationService.marcarComoFavorita(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<Map<String, String>> eliminarDeFavoritas(@Valid @RequestBody FavoriteConvocationRequest request) {
        Map<String, String> response = convocationService.eliminarDeFavoritas(request);
        return ResponseEntity.ok(response);
    }

    /* FUNCIONALIDADES NUEVAS PARA CUMPLIR EL ACRONIMO CRUD */

    // 1. Obtener convocatoria por ID
    @GetMapping("/{id}")
    public ResponseEntity<ConvocationResponse> obtenerConvocatoriaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(convocationService.obtenerConvocatoriaPorId(id));
    }

    // 2. Eliminar convocatoria (requiere usuarioId como parámetro)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarConvocatoria(
            @PathVariable Long id,
            @RequestParam Long usuarioId
    ) {
        Map<String, String> response = convocationService.eliminarConvocatoria(id, usuarioId);
        return ResponseEntity.ok(response);
    }

    // Endpoint para listar TODAS, incluidas expiradas
    @GetMapping("/all")
    public ResponseEntity<List<ConvocationResponse>> listarTodas() {
        return ResponseEntity.ok(convocationService.listarTodas());
    }
}
