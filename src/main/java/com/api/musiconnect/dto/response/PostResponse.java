package com.api.musiconnect.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
    Long postId,
    String contenido,
    String tipo,
    LocalDateTime fechaPublicacion,
    String autor,
    List<ComentarioResponse> comentarios,
    String message
) {}
