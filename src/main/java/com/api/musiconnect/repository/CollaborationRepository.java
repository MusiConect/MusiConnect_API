package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.Collaboration;
import com.api.musiconnect.model.enums.CollaborationStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {
    List<Collaboration> findByEstadoIn(List<CollaborationStatus> estados);

}
