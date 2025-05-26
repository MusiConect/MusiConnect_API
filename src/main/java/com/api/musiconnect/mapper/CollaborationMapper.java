package com.api.musiconnect.mapper;

import com.api.musiconnect.dto.request.CollaborationRequest;
import com.api.musiconnect.dto.response.CollaborationResponse;
import com.api.musiconnect.model.entity.Band;
import com.api.musiconnect.model.entity.Collaboration;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.CollaborationStatus;

public class CollaborationMapper {

    // Para crear una nueva entidad desde el request
    public static Collaboration toEntity(CollaborationRequest request, User usuario, Band banda) {
        return Collaboration.builder()
            .titulo(request.titulo())
            .descripcion(request.descripcion())
            .fechaInicio(request.fechaInicio())
            .fechaFin(request.fechaFin())
            .estado(CollaborationStatus.PENDIENTE) // SEGUN EL US16: siempre inicia como pendiente
            .usuario(usuario)
            .banda(banda)
            .build();
    }

    // Para retornar los datos al cliente
    public static CollaborationResponse toResponse(Collaboration entity) {
        return new CollaborationResponse(
            entity.getColaboracionId(),
            entity.getTitulo(),
            entity.getDescripcion(),
            entity.getFechaInicio(),
            entity.getFechaFin(),
            entity.getEstado(),
            entity.getUsuario().getNombreArtistico(),
            entity.getBanda().getNombre()
        );
    }
}
