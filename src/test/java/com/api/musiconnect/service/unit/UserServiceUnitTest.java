package com.api.musiconnect.service.unit;

import com.api.musiconnect.dto.request.LoginRequest;
import com.api.musiconnect.dto.request.UserAvailabilityRequest;
import com.api.musiconnect.dto.request.UserRequest;
import com.api.musiconnect.dto.request.UserUpdateRequest;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.entity.Role;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import com.api.musiconnect.model.enums.RoleEnum;
import com.api.musiconnect.repository.MusicGenreRepository;
import com.api.musiconnect.repository.UserRepository;
import com.api.musiconnect.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MusicGenreRepository musicGenreRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private com.api.musiconnect.repository.RoleRepository roleRepository;

    @Mock
    private com.api.musiconnect.security.JwtUtil jwtUtil;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private com.api.musiconnect.service.auth.AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("CP01 - Actualiza correctamente el usuario con datos válidos")
    void CP01_updateUser_successfulUpdate() {
        Long userId = 1L;
        User user = new User();
        UserUpdateRequest request = new UserUpdateRequest(
                "new@mail.com",
                "bio",
                "Peru",
                "guitarra",
                List.of("ROCK"),
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndUserIdNot("new@mail.com", userId)).thenReturn(false);
        when(musicGenreRepository.findAllByNombreIn(List.of(MusicGenreEnum.ROCK)))
                .thenReturn(List.of(
                        MusicGenre.builder()
                                .generoId(1L)
                                .nombre(MusicGenreEnum.ROCK)
                                .build()
                ));

        Map<String, String> response = userService.updateUser(userId, request);

        assertEquals("Perfil actualizado correctamente", response.get("message"));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("CP02 - Lanza excepción si el usuario no existe")
    void CP02_updateUser_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateUser(1L, mock(UserUpdateRequest.class)));
    }

    @Test
    @DisplayName("CP03 - Lanza excepción si el email ya está en uso")
    void CP03_updateUser_emailAlreadyInUse() {
        User user = new User();
        UserUpdateRequest request = new UserUpdateRequest(
                "existing@mail.com",
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndUserIdNot("existing@mail.com", 1L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () ->
                userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("CP04 - Ignora los campos nulos y actualiza sin errores")
    void CP04_updateUser_ignoreNullFields() {
        User user = new User();
        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Map<String, String> response = userService.updateUser(1L, request);

        assertEquals("Perfil actualizado correctamente", response.get("message"));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("CP05 - Lanza excepción si se envía un género musical inválido")
    void CP05_updateUser_invalidGenre() {
        User user = new User();
        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                null,
                null,
                List.of("INVALID"),  // este género no existe en el enum
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () ->
                userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("CP06 - Actualiza correctamente la disponibilidad del usuario")
    void CP06_updateAvailability_success() {
        User user = new User();
        UserAvailabilityRequest request = new UserAvailabilityRequest(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Map<String, String> response = userService.updateAvailability(1L, request);

        assertEquals("Estado de disponibilidad actualizado a disponible.", response.get("message"));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("CP07 - Lanza excepción si el usuario no existe al actualizar disponibilidad")
    void CP07_updateAvailability_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateAvailability(1L, new UserAvailabilityRequest(true)));
    }

    @Test
    @DisplayName("CP08 - Registro exitoso con rol MUSICO y géneros válidos")
    void CP08_register_successful() {
        var request = new UserRequest(
                "correo@musico.com",
                "123",
                "Artista",
                "Guitarra",
                "Mi bio",
                "Perú",
                true,
                1L,
                List.of("ROCK")
        );

        Role role = new Role();
        role.setRoleId(1L);
        role.setName(RoleEnum.MUSICO);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(musicGenreRepository.findAllByNombreIn(List.of(MusicGenreEnum.ROCK)))
                .thenReturn(List.of(MusicGenre.builder()
                        .generoId(1L)
                        .nombre(MusicGenreEnum.ROCK)
                        .build()));
        when(passwordEncoder.encode("123")).thenReturn("hashed123");
        when(jwtUtil.generateToken("correo@musico.com")).thenReturn("jwt-token");

        var response = authService.register(request);

        assertEquals("Registro exitoso", response.mensaje());
        assertEquals("Artista", response.nombreArtistico());
        assertEquals("jwt-token", response.token());
    }

    @Test
    @DisplayName("CP09 - Lanza excepción si el email ya está registrado")
    void CP09_register_emailExists() {
        var request = new UserRequest(
                "correo@yaexiste.com", "123", "Artista", "Bajo", "bio", "Chile",
                true, 1L, List.of("ROCK")
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("CP10 - Lanza excepción si el rol no existe")
    void CP10_register_roleNotFound() {
        var request = new UserRequest(
                "nuevo@correo.com", "123", "Artista", "Bajo", "bio", "Perú",
                true, 99L, List.of("ROCK")
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("CP11 - Lanza excepción si el rol no es MUSICO ni PRODUCTOR")
    void CP11_register_invalidRole() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setName(RoleEnum.ADMIN); // no permitido

        var request = new UserRequest(
                "admin@correo.com", "123", "Admin", "Piano", "bio", "México",
                true, 1L, List.of("ROCK")
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        assertThrows(BusinessRuleException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("CP12 - Registro exitoso sin géneros musicales (opcional)")
    void CP12_register_noGenres() {
        var request = new UserRequest(
                "prod@correo.com", "123", "Productor", "Sintetizador", "bio", "Argentina",
                true, 1L, List.of()
        );

        Role role = new Role();
        role.setRoleId(1L);
        role.setName(RoleEnum.PRODUCTOR);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("123")).thenReturn("hash");
        when(jwtUtil.generateToken("prod@correo.com")).thenReturn("jwt");

        var response = authService.register(request);

        assertEquals("jwt", response.token());
        assertEquals("Productor", response.nombreArtistico());
    }

    @Test
    @DisplayName("CP13 - Lanza excepción si hay géneros inválidos")
    void CP13_register_invalidGenre() {
        var request = new UserRequest(
                "nuevo@correo.com", "123", "Artista", "Bajo", "bio", "España",
                true, 1L, List.of("INVALID")
        );

        Role role = new Role();
        role.setRoleId(1L);
        role.setName(RoleEnum.MUSICO);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        // no se encuentra ningún género válido
        when(musicGenreRepository.findAllByNombreIn(List.of())).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("CP14 - Login exitoso con credenciales válidas")
    void CP14_login_successful() {
        var request = new LoginRequest("email@valido.com", "123");

        User user = new User();
        user.setEmail("email@valido.com");
        user.setPassword("encodedPass");
        user.setUserId(1L);
        user.setNombreArtistico("TestUser");

        when(userRepository.findByEmail("email@valido.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("email@valido.com")).thenReturn("token-valido");

        var response = authService.login(request);

        assertEquals("token-valido", response.token());
        assertEquals("Login exitoso", response.mensaje());
        assertEquals("TestUser", response.nombreArtistico());
    }

    @Test
    @DisplayName("CP15 - Lanza excepción si el email no existe al hacer login")
    void CP15_login_emailNotFound() {
        var request = new LoginRequest("noexiste@mail.com", "123");
        when(userRepository.findByEmail("noexiste@mail.com")).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("CP16 - Lanza excepción si la contraseña es incorrecta")
    void CP16_login_wrongPassword() {
        var request = new LoginRequest("user@mail.com", "wrong");

        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword("encodedPass");

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> authService.login(request));
    }
}
