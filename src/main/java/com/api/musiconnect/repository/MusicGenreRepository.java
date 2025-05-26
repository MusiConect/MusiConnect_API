package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MusicGenreRepository extends JpaRepository<MusicGenre, Long> {
    List<MusicGenre> findAllByNombreIn(List<MusicGenreEnum> nombres);
    boolean existsByNombre(MusicGenreEnum nombre);
}
