package com.api.musiconnect.mapper;

import com.api.musiconnect.dto.response.FollowResponse;
import com.api.musiconnect.model.entity.Follow;

public class FollowMapper {

    public static FollowResponse toResponse(Follow follow) {
        String seguidoNombre = follow.getFollowedUser() != null
                ? follow.getFollowedUser().getNombreArtistico()
                : follow.getFollowedBand().getNombre();

        String tipoSeguido = follow.getFollowedUser() != null ? "Usuario" : "Banda";

        return new FollowResponse(
            follow.getFollowId(),
            follow.getFollower().getNombreArtistico(),
            seguidoNombre,
            tipoSeguido,
            follow.getFechaSeguimiento(),
            "Ahora sigues a " + seguidoNombre + "."
        );
    }
}
