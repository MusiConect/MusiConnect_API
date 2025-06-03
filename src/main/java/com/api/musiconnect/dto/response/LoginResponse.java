package com.api.musiconnect.dto.response;

public record LoginResponse(
        String mensaje,
        Long userId,
        String nombreArtistico,
        String token
) {}
