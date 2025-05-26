package com.api.musiconnect.mapper;

import com.api.musiconnect.dto.request.ConvocationRequest;
import com.api.musiconnect.dto.response.ConvocationResponse;
import com.api.musiconnect.model.entity.Convocation;
import com.api.musiconnect.model.entity.User;

public class ConvocationMapper {

    public static Convocation toEntity(ConvocationRequest request, User usuario) {
        return Convocation.builder()
                .usuario(usuario)
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .fechaLimite(request.fechaLimite())
                .activa(true)
                .build();
    }

    public static ConvocationResponse toResponse(Convocation convocatoria) {
        return new ConvocationResponse(
                convocatoria.getConvocationId(),
                convocatoria.getTitulo(),
                convocatoria.getDescripcion(),
                convocatoria.getFechaLimite(),
                convocatoria.getUsuario().getNombreArtistico(),
                "Convocatoria creada exitosamente."
        );
    }
}
