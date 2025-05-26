package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.Band;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BandRepository extends JpaRepository<Band, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByMiembrosUserIdAndBandId(Long userId, Long bandId);
}
