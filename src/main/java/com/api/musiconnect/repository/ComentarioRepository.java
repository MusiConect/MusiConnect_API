package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.Comentario;
import com.api.musiconnect.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByPost(Post post);
}
