package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.ConvocationFavorite;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.entity.Convocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConvocationFavoriteRepository extends JpaRepository<ConvocationFavorite, Long> {

    List<ConvocationFavorite> findByUsuarioUserId(Long userId);

    Optional<ConvocationFavorite> findByUsuarioAndConvocatoria(User usuario, Convocation convocatoria);

    boolean existsByUsuarioAndConvocatoria(User usuario, Convocation convocatoria);
}
