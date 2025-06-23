package com.api.musiconnect.service;

import com.api.musiconnect.dto.request.UserAvailabilityRequest;
import com.api.musiconnect.dto.request.UserUpdateRequest;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import com.api.musiconnect.repository.MusicGenreRepository;
import com.api.musiconnect.repository.UserRepository;
import com.api.musiconnect.dto.response.UserResponse;
import com.api.musiconnect.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MusicGenreRepository musicGenreRepository;


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

    /* NUEVAS FUNCIONALIDADES PARA EL ACRONIMO CRUD */

    // 1. Obtener todos los usuarios
    public List<UserResponse> obtenerTodosLosUsuarios() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    // 2. Obtener usuario por ID
    public UserResponse obtenerUsuarioPorId(Long id) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
        return UserMapper.toResponse(usuario);
    }

    // 3. Obtener usuario por nombre artístico
    public UserResponse obtenerPorNombreArtistico(String nombreArtistico) {
        User usuario = userRepository.findByNombreArtisticoIgnoreCase(nombreArtistico)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ese nombre artístico."));
        return UserMapper.toResponse(usuario);
    }

    // 4. Obtener usuarios por género musical
    public List<UserResponse> obtenerPorGeneroMusical(String genero) {
        MusicGenreEnum generoEnum;
        try {
            generoEnum = MusicGenreEnum.valueOf(genero.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Género musical inválido.");
        }

        return userRepository.findAll().stream()
                .filter(user -> user.getGenerosMusicales().stream()
                        .anyMatch(g -> g.getNombre().equals(generoEnum)))
                .map(UserMapper::toResponse)
                .toList();
    }

    // 5. Eliminar usuario
    @Transactional
    public Map<String, String> eliminarUsuario(Long id) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        userRepository.delete(usuario);

        return Map.of("message", "Usuario eliminado exitosamente.");
    }
}
