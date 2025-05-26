package com.api.musiconnect.dto.response;

public record FollowedProfileResponse(
    Long id,
    String nombre,
    String tipo, // "Usuario" o "Banda"
    Boolean disponible,
    String ubicacion,
    String imagen
) {}
