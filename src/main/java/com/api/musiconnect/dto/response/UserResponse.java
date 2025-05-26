package com.api.musiconnect.dto.response;

import java.util.List;

public record UserResponse(
    Long userId,
    String email,
    String nombreArtistico,
    String instrumentos,
    String bio,
    String ubicacion,
    Boolean disponibilidad,
    String role,
    List<String> generosMusicales,
    String message
) {}
