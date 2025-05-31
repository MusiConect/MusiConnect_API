package com.api.musiconnect.service;

import com.api.musiconnect.dto.request.LoginRequest;
import com.api.musiconnect.dto.request.UserAvailabilityRequest;
import com.api.musiconnect.dto.request.UserRequest;
import com.api.musiconnect.dto.request.UserUpdateRequest;
import com.api.musiconnect.dto.response.LoginResponse;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MusicGenreRepository musicGenreRepository;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Ya existe un perfil registrado con este correo.");
        }


        
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new BusinessRuleException("Rol no encontrado."));


        if (!role.getName().equals(RoleEnum.MUSICO) && !role.getName().equals(RoleEnum.PRODUCTOR)) {
            throw new BusinessRuleException("Solo se permiten roles MÚSICO o PRODUCTOR para el registro.");
        }

    List<MusicGenre> generos = List.of(); // Lista vacía por defecto

    if (request.generosMusicales() != null && !request.generosMusicales().isEmpty()) {
        List<MusicGenreEnum> generosEnum;
        try {
            generosEnum = request.generosMusicales().stream()
                .map(nombre -> MusicGenreEnum.valueOf(nombre.toUpperCase()))
                .toList();
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Género musical inválido.");
        }

        generos = musicGenreRepository.findAllByNombreIn(generosEnum);
        if (generos.size() != generosEnum.size()) {
            throw new BusinessRuleException("Género musical inválido.");
        }
    }


        var user = UserMapper.toEntity(request, role, generos);
        if (user.getDisponibilidad() == null) {
            user.setDisponibilidad(true ); // Por defecto disponible
        }
        var savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessRuleException("Credenciales inválidas."));

        if (!user.getPassword().equals(request.password())) {
            throw new BusinessRuleException("Credenciales inválidas.");
        }

        return new LoginResponse("Inicio de sesión exitoso.", user.getUserId(), user.getNombreArtistico());
    }


    @Transactional
    public Map<String, String> updateUser(Long userId, UserUpdateRequest request) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado."));

        // Validar y actualizar email
        if (request.email() != null && !request.email().isBlank()) {
            if (userRepository.existsByEmailAndUserIdNot(request.email(), userId)) {
                throw new BusinessRuleException("Ya existe un perfil registrado con este correo.");
            }
            usuario.setEmail(request.email());
        }

        // Validar y actualizar bio
        if (request.bio() != null && !request.bio().isBlank()) {
            usuario.setBio(request.bio());
        }

        // Validar y actualizar ubicación
        if (request.ubicacion() != null && !request.ubicacion().isBlank()) {
            usuario.setUbicacion(request.ubicacion());
        }

        // Validar y actualizar instrumentos
        if (request.instrumentos() != null && !request.instrumentos().isBlank()) {
            usuario.setInstrumentos(request.instrumentos());
        }

        // Validar y actualizar disponibilidad
        if (request.disponibilidad() != null) {
            usuario.setDisponibilidad(request.disponibilidad());
        }

        // Validar y actualizar géneros musicales
        if (request.generos() != null && !request.generos().isEmpty()) {
            List<MusicGenreEnum> generosEnum;
            try {
                generosEnum = request.generos().stream()
                        .map(g -> MusicGenreEnum.valueOf(g.toUpperCase()))
                        .toList();
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Género musical inválido.");
            }

            List<MusicGenre> generos = musicGenreRepository.findAllByNombreIn(generosEnum);
            if (generos.size() != generosEnum.size()) {
                throw new BusinessRuleException("Género musical inválido.");
            }

            usuario.setGenerosMusicales(generos);
        }

        userRepository.save(usuario);

        return Map.of("message", "Perfil actualizado correctamente");
    }
    
    @Transactional
    public Map<String, String> updateAvailability(Long userId, UserAvailabilityRequest request) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado."));

        usuario.setDisponibilidad(request.disponibilidad());
        userRepository.save(usuario);

        String estado = request.disponibilidad() ? "disponible" : "no disponible";
        return Map.of("message", "Estado de disponibilidad actualizado a " + estado + ".");
    }

}
