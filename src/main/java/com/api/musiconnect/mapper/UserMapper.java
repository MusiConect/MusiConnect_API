package com.api.musiconnect.mapper;

import com.api.musiconnect.dto.request.UserRequest;
import com.api.musiconnect.dto.response.UserResponse;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.entity.Role;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.MusicGenreEnum;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toEntity(UserRequest dto, Role role, List<MusicGenre> generos) {
        return User.builder()
            .email(dto.email())
            .password(dto.password())
            .nombreArtistico(dto.nombreArtistico())
            .instrumentos(dto.instrumentos())
            .bio(dto.bio())
            .ubicacion(dto.ubicacion())
            .disponibilidad(dto.disponibilidad())
            .role(role)
            .generosMusicales(generos)
            .build();
    }

    public static UserResponse toResponse(User user) {
        List<String> generos = user.getGenerosMusicales().stream()
            .map(g -> g.getNombre().name())
            .collect(Collectors.toList());

        return new UserResponse(
            user.getUserId(),
            user.getEmail(),
            user.getNombreArtistico(),
            user.getInstrumentos(),
            user.getBio(),
            user.getUbicacion(),
            user.getDisponibilidad(),
            user.getRole().getName().name(),  // si `name` es `RoleEnum`, sino solo `.getName()`
            generos,
            "Perfil creado exitosamente."
        );
    }
}