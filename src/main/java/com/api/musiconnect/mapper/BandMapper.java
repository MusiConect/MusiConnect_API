package com.api.musiconnect.mapper;

import java.util.List;

import com.api.musiconnect.dto.request.BandRequest;
import com.api.musiconnect.dto.response.BandResponse;
import com.api.musiconnect.model.entity.Band;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.entity.User;

public class BandMapper {

    public static Band toEntity(BandRequest dto, User admin, List<MusicGenre> generos) {
        return Band.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .administrador(admin)
                .generosMusicales(generos)
                .build();
    }

    public static BandResponse toResponse(Band band) {
        List<String> generos = band.getGenerosMusicales().stream()
            .map(g -> g.getNombre().name())
            .toList();

        return new BandResponse(
            band.getBandId(),
            band.getNombre(),
            band.getDescripcion(),
            band.getAdministrador().getNombreArtistico(),
            generos,
            "Banda creada exitosamente."
        );
    }

}
