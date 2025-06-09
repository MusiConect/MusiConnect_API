package com.api.musiconnect.service;

import com.api.musiconnect.dto.request.ComentarioRequest;
import com.api.musiconnect.dto.request.PostRequest;
import com.api.musiconnect.dto.request.PostUpdateRequest;
import com.api.musiconnect.dto.response.ComentarioResponse;
import com.api.musiconnect.dto.response.PostResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.mapper.ComentarioMapper;
import com.api.musiconnect.mapper.PostMapper;
import com.api.musiconnect.model.entity.Comentario;
import com.api.musiconnect.model.entity.Post;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.repository.ComentarioRepository;
import com.api.musiconnect.repository.PostRepository;
import com.api.musiconnect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ComentarioRepository comentarioRepository;

    @Transactional
    public PostResponse crearPost(PostRequest request) {
        User usuario = userRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Post post = PostMapper.toEntity(request, usuario);
        postRepository.save(post);

        return PostMapper.toResponse(post, List.of());
    }

    @Transactional
    public PostResponse editarPost(Long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada."));

        if (!post.getUsuario().getUserId().equals(request.usuarioId())) {
            throw new BusinessRuleException("No tiene permisos para editar esta publicación.");
        }

        post.setContenido(request.contenido());
        postRepository.save(post);

        List<ComentarioResponse> comentarios = comentarioRepository.findByPost(post).stream()
                .map(ComentarioMapper::toResponse)
                .toList();

        return new PostResponse(
                post.getPostId(),
                post.getContenido(),
                post.getTipo().name(),
                post.getFechaPublicacion(),
                post.getUsuario().getNombreArtistico(),
                comentarios,
                "Publicación actualizada correctamente."
        );
    }

    @Transactional
    public ComentarioResponse comentarPost(Long postId, ComentarioRequest request) {
        User usuario = userRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada."));

        Comentario comentario = Comentario.builder()
                .contenido(request.contenido())
                .fechaComentario(LocalDateTime.now())
                .usuario(usuario)
                .post(post)
                .build();

        return ComentarioMapper.toResponse(comentarioRepository.save(comentario));
    }
    
    public List<ComentarioResponse> listarComentarios(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada."));

        return comentarioRepository.findByPost(post).stream()
                .map(ComentarioMapper::toResponse)
                .toList();
    }

}
