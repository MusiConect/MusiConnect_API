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


}
