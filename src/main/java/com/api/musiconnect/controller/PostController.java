package com.api.musiconnect.controller;

import com.api.musiconnect.dto.request.ComentarioRequest;
import com.api.musiconnect.dto.request.PostRequest;
import com.api.musiconnect.dto.request.PostUpdateRequest;
import com.api.musiconnect.dto.response.ComentarioResponse;
import com.api.musiconnect.dto.response.PostResponse;
import com.api.musiconnect.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> crearPost(@Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.crearPost(request));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> editarPost(@PathVariable Long postId,
        @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(postService.editarPost(postId, request));
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<ComentarioResponse> comentarPost(@PathVariable Long postId,
        @Valid @RequestBody ComentarioRequest request) {
        return ResponseEntity.ok(postService.comentarPost(postId, request));
    }
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<ComentarioResponse>> listarComentarios(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.listarComentarios(postId));
    }

    // 1. Listar todos los posts
    @GetMapping
    public ResponseEntity<List<PostResponse>> listarPosts() {
        return ResponseEntity.ok(postService.listarTodosLosPosts());
    }

    // 2. Obtener un post por su ID
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> obtenerPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.obtenerPostPorId(postId));
    }

    // 3. Eliminar publicación
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> eliminarPost(@PathVariable Long postId, @RequestParam Long usuarioId) {
        postService.eliminarPost(postId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // 4. Editar comentario
    @PutMapping("/comments/{comentarioId}")
    public ResponseEntity<ComentarioResponse> editarComentario(@PathVariable Long comentarioId,
            @Valid @RequestBody ComentarioRequest request) {
        return ResponseEntity.ok(postService.editarComentario(comentarioId, request));
    }

    // 5. Eliminar comentario
    @DeleteMapping("/comments/{comentarioId}")
    public ResponseEntity<Void> eliminarComentario(@PathVariable Long comentarioId, @RequestParam Long usuarioId) {
        postService.eliminarComentario(comentarioId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
