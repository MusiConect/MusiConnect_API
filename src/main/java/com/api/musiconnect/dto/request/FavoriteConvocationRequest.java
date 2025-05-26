package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.NotNull;

public record FavoriteConvocationRequest(

    @NotNull(message = "Debe especificar el ID del usuario.")
    Long usuarioId,

    @NotNull(message = "Debe especificar el ID de la convocatoria.")
    Long convocatoriaId
) {}
