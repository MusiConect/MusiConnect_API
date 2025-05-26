package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.Role;
import com.api.musiconnect.model.enums.RoleEnum;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByName(RoleEnum name);

}

