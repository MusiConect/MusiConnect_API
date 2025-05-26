package com.api.musiconnect.mapper;

import com.api.musiconnect.dto.request.PostRequest;
import com.api.musiconnect.dto.response.PostResponse;
import com.api.musiconnect.model.entity.Post;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.PostTipo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class PostMapper {

    public static Post toEntity(PostRequest request, User usuario) {
        return Post.builder()
                .usuario(usuario)
                .contenido(request.contenido())
                .tipo(PostTipo.valueOf(request.tipo().toUpperCase()))
                .fechaPublicacion(LocalDateTime.now())
                .comentarios(Collections.emptyList())
                .build();
    }

    public static PostResponse toResponse(Post post, List<com.api.musiconnect.dto.response.ComentarioResponse> comentarios) {
        return new PostResponse(
                post.getPostId(),
                post.getContenido(),
                post.getTipo().name(),
                post.getFechaPublicacion(),
                post.getUsuario().getNombreArtistico(),
                comentarios,
                "Publicación creada exitosamente."
        );
    }
}
