package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.Collaboration;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.CollaborationStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {
    List<Collaboration> findByEstadoIn(List<CollaborationStatus> estados);
    List<Collaboration> findByUsuario_NombreArtisticoIgnoreCase(String nombreArtistico);
    boolean existsByTituloIgnoreCase(String titulo);

}
