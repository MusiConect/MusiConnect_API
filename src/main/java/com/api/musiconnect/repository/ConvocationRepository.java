package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.Convocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvocationRepository extends JpaRepository<Convocation, Long> {
}
