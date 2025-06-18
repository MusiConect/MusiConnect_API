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

        // Validación de contenido vacío
        if (request.contenido() == null || request.contenido().trim().isEmpty()) {
                throw new BusinessRuleException("El contenido de la publicación no puede estar vacío.");
        }
        if (request.contenido().length() > 500) {
        throw new BusinessRuleException("El contenido de la publicación excede el límite de 500 caracteres.");
        }

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

        if (request.contenido() == null || request.contenido().trim().isEmpty()) {
        throw new BusinessRuleException("El contenido del comentario no puede estar vacío.");
        }
        if (request.contenido().length() > 300) {
        throw new BusinessRuleException("El contenido del comentario no debe exceder los 300 caracteres.");
        }
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


        /* NUEVAS FUNCIONALIDADES PARA EL ACRONIMO CRUD */

        // 1. Listar todos los posts
        public List<PostResponse> listarTodosLosPosts() {
        return postRepository.findAll().stream()
                .map(post -> {
                        List<ComentarioResponse> comentarios = comentarioRepository.findByPost(post).stream()
                                .map(ComentarioMapper::toResponse)
                                .toList();
                        return PostMapper.toResponse(post, comentarios);
                })
                .toList();
        }

        // 2. Obtener un post por ID
        public PostResponse obtenerPostPorId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada."));

        List<ComentarioResponse> comentarios = comentarioRepository.findByPost(post).stream()
                .map(ComentarioMapper::toResponse)
                .toList();

        return PostMapper.toResponse(post, comentarios);
        }

        // 3. Eliminar publicación
        @Transactional
        public void eliminarPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada."));

        if (!post.getUsuario().getUserId().equals(usuarioId)) {
                throw new BusinessRuleException("No tiene permisos para eliminar esta publicación.");
        }

        comentarioRepository.deleteAll(post.getComentarios()); // elimina comentarios asociados
        postRepository.delete(post);
        }

        // 4. Editar comentario
        @Transactional
        public ComentarioResponse editarComentario(Long comentarioId, ComentarioRequest request) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado."));

        if (!comentario.getUsuario().getUserId().equals(request.usuarioId())) {
                throw new BusinessRuleException("No tiene permisos para editar este comentario.");
        }

        comentario.setContenido(request.contenido());
        return ComentarioMapper.toResponse(comentarioRepository.save(comentario));
        }

        // 5. Eliminar comentario
        @Transactional
        public void eliminarComentario(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado."));

        if (!comentario.getUsuario().getUserId().equals(usuarioId)) {
                throw new BusinessRuleException("No tiene permisos para eliminar este comentario.");
        }

        comentarioRepository.delete(comentario);
        }
}
