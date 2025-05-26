package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.*;

public record ComentarioRequest(

    @NotNull(message = "Debe especificar el ID del autor.")
    Long usuarioId,

    @NotBlank(message = "El comentario no puede estar vacío.")
    @Size(max = 300, message = "El comentario no puede superar los 300 caracteres.")
    String contenido
) {}
