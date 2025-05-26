package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.*;

public record PostUpdateRequest(

    @NotNull(message = "Debe especificar el ID del autor.")
    Long usuarioId,

    @NotBlank(message = "El contenido no puede estar vacío.")
    @Size(max = 500, message = "El contenido no puede superar los 500 caracteres.")
    String contenido
) {}
