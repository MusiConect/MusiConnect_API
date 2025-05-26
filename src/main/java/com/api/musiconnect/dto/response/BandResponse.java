package com.api.musiconnect.dto.response;

import java.util.List;

public record BandResponse(
    Long bandId,
    String nombre,
    String descripcion,
    String administradorNombreArtistico,
    List<String> generosMusicales,
    String message
) {}
