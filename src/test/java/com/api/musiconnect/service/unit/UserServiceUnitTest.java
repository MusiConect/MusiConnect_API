package com.api.musiconnect.service.unit;

import com.api.musiconnect.dto.request.UserRequest;
import com.api.musiconnect.dto.request.UserUpdateRequest;
import com.api.musiconnect.dto.response.UserResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1L);
        user.setEmail("original@example.com");
    }

    // Aquí agregaremos las pruebas en los siguientes pasos
    @Test
    @DisplayName("CP01 - Actualizar todos los campos correctamente")
    void updateUser_allValidFields_updatedSuccessfully() {
        UserUpdateRequest request = new UserUpdateRequest(
                "nuevo@example.com", "Nueva bio", "Lima", "Guitarra",
                List.of("ROCK", "JAZZ"), true
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndUserIdNot("nuevo@example.com", 1L)).thenReturn(false);
        when(musicGenreRepository.findAllByNombreIn(List.of(MusicGenreEnum.ROCK, MusicGenreEnum.JAZZ)))
                .thenReturn(List.of(new MusicGenre(), new MusicGenre()));

        Map<String, String> result = userService.updateUser(1L, request);

        assertEquals("Perfil actualizado correctamente", result.get("message"));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("CP02 - Usuario no encontrado")
    void updateUser_userNotFound_exceptionThrown() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        UserUpdateRequest request = mock(UserUpdateRequest.class);

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("CP03 - Email duplicado")
    void updateUser_emailDuplicado_exceptionThrown() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndUserIdNot("duplicado@example.com", 1L)).thenReturn(true);

        UserUpdateRequest request = new UserUpdateRequest("duplicado@example.com", null, null, null, null, null);

        assertThrows(BusinessRuleException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("CP04 - Género musical inválido (enum no existe)")
    void updateUser_invalidGenreEnum_exceptionThrown() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserUpdateRequest request = new UserUpdateRequest(null, null, null, null, List.of("INVALIDO"), null);

        assertThrows(BusinessRuleException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("CP05 - Géneros no encontrados en base de datos")
    void updateUser_invalidGenresFromDB_exceptionThrown() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserUpdateRequest request = new UserUpdateRequest(null, null, null, null, List.of("ROCK", "JAZZ"), null);

        when(musicGenreRepository.findAllByNombreIn(List.of(MusicGenreEnum.ROCK, MusicGenreEnum.JAZZ)))
                .thenReturn(List.of(new MusicGenre())); // solo 1 encontrado

        assertThrows(BusinessRuleException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("CP06 - No actualizar nada (request vacío)")
    void updateUser_emptyRequest_nothingUpdated() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserUpdateRequest request = new UserUpdateRequest(null, null, null, null, null, null);

        Map<String, String> result = userService.updateUser(1L, request);

        assertEquals("Perfil actualizado correctamente", result.get("message"));
        verify(userRepository).save(user);
    }
}
