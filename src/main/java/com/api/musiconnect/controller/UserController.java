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

    /* NUEVAS FUNCIONALIDADES PARA EL ACRONIMO CRUD */

    // 1. Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<?> obtenerTodos() {
        return ResponseEntity.ok(userService.obtenerTodosLosUsuarios());
    }

    // 2. Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(userService.obtenerUsuarioPorId(id));
    }

    // 3. Obtener usuario por nombre artístico
    @GetMapping("/nombre-artistico/{nombre}")
    public ResponseEntity<?> obtenerPorNombreArtistico(@PathVariable String nombre) {
        return ResponseEntity.ok(userService.obtenerPorNombreArtistico(nombre));
    }

    // 4. Obtener usuarios por género musical
    @GetMapping("/genero/{genero}")
    public ResponseEntity<?> obtenerPorGenero(@PathVariable String genero) {
        return ResponseEntity.ok(userService.obtenerPorGeneroMusical(genero));
    }

    // 5. Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(userService.eliminarUsuario(id));
    }

}
