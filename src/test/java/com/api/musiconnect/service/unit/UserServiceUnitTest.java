package com.api.musiconnect.service.unit;

import com.api.musiconnect.dto.request.UserRequest;
import com.api.musiconnect.dto.response.UserResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.mapper.UserMapper;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.entity.Role;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import com.api.musiconnect.model.enums.RoleEnum;
import com.api.musiconnect.repository.MusicGenreRepository;
import com.api.musiconnect.repository.RoleRepository;
import com.api.musiconnect.repository.UserRepository;
import com.api.musiconnect.service.UserService;
import com.api.musiconnect.service.auth.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MusicGenreRepository musicGenreRepository;

    @InjectMocks
    private UserService userService;

        @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Aquí agregaremos las pruebas en los siguientes pasos

}
