package com.api.musiconnect.mapper;

import com.api.musiconnect.dto.response.ComentarioResponse;
import com.api.musiconnect.model.entity.Comentario;

public class ComentarioMapper {

    public static ComentarioResponse toResponse(Comentario comentario) {
        return new ComentarioResponse(
                comentario.getComentarioId(),
                comentario.getContenido(),
                comentario.getFechaComentario(),
                comentario.getUsuario().getNombreArtistico()
        );
    }
}
