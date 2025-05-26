package com.api.musiconnect.dto.response;

import java.time.LocalDate;

public record ConvocationResponse(
    Long convocationId,
    String titulo,
    String descripcion,
    LocalDate fechaLimite,
    String creadorNombreArtistico,
    String message
) {}
