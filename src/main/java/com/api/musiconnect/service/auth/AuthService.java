package com.api.musiconnect.service.auth;

import com.api.musiconnect.dto.request.LoginRequest;
import com.api.musiconnect.dto.request.UserRequest;
import com.api.musiconnect.dto.response.LoginResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.model.entity.*;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import com.api.musiconnect.repository.*;
import com.api.musiconnect.security.JwtUtil;
import com.api.musiconnect.mapper.UserMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MusicGenreRepository musicGenreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse register(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Ya existe un perfil registrado con este correo.");
        }

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new BusinessRuleException("Rol no encontrado."));

        if (!role.getName().name().equals("MUSICO") && !role.getName().name().equals("PRODUCTOR")) {
            throw new BusinessRuleException("Solo se permiten roles MÚSICO o PRODUCTOR para el registro.");
        }

        List<MusicGenre> generos = List.of();

        if (request.generosMusicales() != null && !request.generosMusicales().isEmpty()) {
            List<MusicGenreEnum> generosEnum = request.generosMusicales().stream()
                    .map(nombre -> MusicGenreEnum.valueOf(nombre.toUpperCase()))
                    .toList();

            generos = musicGenreRepository.findAllByNombreIn(generosEnum);

            if (generos.size() != generosEnum.size()) {
                throw new BusinessRuleException("Género musical inválido.");
            }
        }

        var user = UserMapper.toEntity(request, role, generos);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getDisponibilidad() == null) user.setDisponibilidad(true); // Se pone disponible por defecto

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse("Registro exitoso", user.getUserId(), user.getNombreArtistico(), token);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessRuleException("Credenciales inválidas."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessRuleException("Credenciales inválidas.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse("Login exitoso", user.getUserId(), user.getNombreArtistico(), token);
    }
}
