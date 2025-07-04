package com.api.musiconnect.service;

import com.api.musiconnect.dto.request.AddMemberRequest;
import com.api.musiconnect.dto.request.BandRequest;
import com.api.musiconnect.dto.request.BandUpdateRequest;
import com.api.musiconnect.dto.response.BandResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.mapper.BandMapper;
import com.api.musiconnect.model.entity.Band;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import com.api.musiconnect.model.enums.RoleEnum;
import com.api.musiconnect.repository.BandRepository;
import com.api.musiconnect.repository.MusicGenreRepository;
import com.api.musiconnect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BandService {

    private final BandRepository bandRepository;
    private final UserRepository userRepository;
    private final MusicGenreRepository musicGenreRepository;

    @Transactional
    public BandResponse crearBanda(BandRequest request) {

        if (bandRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessRuleException("Ya existe una banda con este nombre.");
        }

        User admin = userRepository.findById(request.adminId())
                .orElseThrow(() -> new BusinessRuleException("Usuario administrador no encontrado."));

        // Validar que el administrador sea PRODUCTOR
        if (!RoleEnum.PRODUCTOR.equals(admin.getRole().getName())) {
            throw new BusinessRuleException("Solo los usuarios con rol PRODUCTOR pueden crear una banda.");
        }

        // Validar disponibilidad del usuario
        if (!Boolean.TRUE.equals(admin.getDisponibilidad())) {
            throw new BusinessRuleException("No puedes crear una banda mientras estés como no disponible.");
        }
        List<MusicGenreEnum> generosEnum;
        try {
            generosEnum = request.generos().stream()
                    .map(nombre -> MusicGenreEnum.valueOf(nombre.toUpperCase()))
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Género musical inválido.");
        }

        List<MusicGenre> generos = musicGenreRepository.findAllByNombreIn(generosEnum);
        if (generos.size() != generosEnum.size()) {
            throw new BusinessRuleException("Género musical inválido.");
        }

        Band banda = BandMapper.toEntity(request, admin, generos);
        return BandMapper.toResponse(bandRepository.save(banda));
    }

    @Transactional
    public Map<String, String> addIntegrante(Long bandId, AddMemberRequest request) {
        Band banda = bandRepository.findById(bandId)
                .orElseThrow(() -> new ResourceNotFoundException("Banda no encontrada."));

        if (!banda.getAdministrador().getUserId().equals(request.adminId())) {
            throw new BusinessRuleException("No tiene permisos para añadir integrantes.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe."));

        // No permitir añadir otro productor a la banda
        if (RoleEnum.PRODUCTOR.equals(user.getRole().getName())) {
            throw new BusinessRuleException("Sólo puede haber un PRODUCTOR por banda (el creador).");
        }

        // Validar disponibilidad del usuario que se desea agregar
        if (!Boolean.TRUE.equals(user.getDisponibilidad())) {
            throw new BusinessRuleException("Este usuario no está disponible para unirse a una banda.");
        }
        if (banda.getMiembros().contains(user)) {
            throw new BusinessRuleException("Este usuario ya es miembro de la banda.");
        }

        banda.getMiembros().add(user);
        bandRepository.save(banda);

        return Map.of("message", "Integrante añadido correctamente");
    }

    @Transactional
    public Map<String, String> updateBand(Long bandId, BandUpdateRequest request) {
        Band banda = bandRepository.findById(bandId)
                .orElseThrow(() -> new ResourceNotFoundException("Banda no encontrada."));

        if (!banda.getAdministrador().getUserId().equals(request.adminId())) {
            throw new BusinessRuleException("No tiene permisos para editar la banda.");
        }

        if (!banda.getNombre().equalsIgnoreCase(request.nombre())
            && bandRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessRuleException("Ese nombre de banda ya está registrado.");
        }

        List<MusicGenreEnum> generosEnum;
        try {
            generosEnum = request.generos().stream()
                    .map(nombre -> MusicGenreEnum.valueOf(nombre.toUpperCase()))
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Género musical inválido.");
        }

        List<MusicGenre> generos = musicGenreRepository.findAllByNombreIn(generosEnum);
        if (generos.size() != generosEnum.size()) {
            throw new BusinessRuleException("Género musical inválido.");
        }

        banda.setNombre(request.nombre());
        banda.setDescripcion(request.descripcion());
        banda.setGenerosMusicales(generos);

        bandRepository.save(banda);

        return Map.of("message", "Información de la banda actualizada correctamente.");
    }

    /* NUEVAS FUNCIONALIDADES PARA EL ACRONIMO CRUD */

    // 1. Obtener todas las bandas
    public List<BandResponse> obtenerTodasLasBandas() {
        return bandRepository.findAll().stream()
                .map(BandMapper::toResponse)
                .toList();
    }

    // 2. Obtener banda por ID
    public BandResponse obtenerBandaPorId(Long id) {
        Band banda = bandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banda no encontrada."));
        return BandMapper.toResponse(banda);
    }

    // 3. Eliminar banda
    @Transactional
    public Map<String, String> eliminarBanda(Long id, Long adminId) {
        Band banda = bandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banda no encontrada."));

        if (!banda.getAdministrador().getUserId().equals(adminId)) {
            throw new BusinessRuleException("No tiene permisos para eliminar esta banda.");
        }

        bandRepository.delete(banda);
        return Map.of("message", "Banda eliminada exitosamente.");
    }

    // 4. Obtener todos los miembros de una banda
    public List<String> obtenerMiembrosDeBanda(Long id) {
        Band banda = bandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banda no encontrada."));

        return banda.getMiembros().stream()
                .map(User::getNombreArtistico)
                .toList();
    }

    // 5. Obtener miembro específico por ID dentro de una banda
    public String obtenerMiembroDeBandaPorId(Long bandId, Long miembroId) {
        Band banda = bandRepository.findById(bandId)
                .orElseThrow(() -> new ResourceNotFoundException("Banda no encontrada."));

        return banda.getMiembros().stream()
                .filter(u -> u.getUserId().equals(miembroId))
                .findFirst()
                .map(User::getNombreArtistico)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no pertenece a la banda."));
    }
}
