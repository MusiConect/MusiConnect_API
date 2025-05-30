package com.api.musiconnect.mapper;

import com.api.musiconnect.dto.request.CollaborationRequest;
import com.api.musiconnect.dto.response.CollaborationResponse;
import com.api.musiconnect.model.entity.Collaboration;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.CollaborationStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollaborationMapper {

    public static Collaboration toEntity(CollaborationRequest request, User usuario) {
        return Collaboration.builder()
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .estado(CollaborationStatus.PENDIENTE)
                .usuario(usuario)
                .build();
    }

    public static CollaborationResponse toResponse(Collaboration colaboracion) {
        List<String> colaboradores = Optional.ofNullable(colaboracion.getColaboradores())
            .orElse(List.of())  // previene null
            .stream()
            .map(User::getNombreArtistico)
            .collect(Collectors.toList());


        return new CollaborationResponse(
                colaboracion.getColaboracionId(),
                colaboracion.getTitulo(),
                colaboracion.getDescripcion(),
                colaboracion.getFechaInicio(),
                colaboracion.getFechaFin(),
                colaboracion.getEstado(),
                colaboracion.getUsuario().getNombreArtistico(),
                colaboradores
        );
    }
}
