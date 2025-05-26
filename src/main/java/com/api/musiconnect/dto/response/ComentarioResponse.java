package com.api.musiconnect.dto.response;

import java.time.LocalDateTime;

public record ComentarioResponse(
    Long comentarioId,
    String contenido,
    LocalDateTime fechaComentario,
    String autor
) {}
