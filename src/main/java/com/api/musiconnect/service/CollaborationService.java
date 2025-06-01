package com.api.musiconnect.service;

import com.api.musiconnect.dto.request.CollaborationRequest;
import com.api.musiconnect.dto.request.CollaborationUpdateRequest;
import com.api.musiconnect.dto.response.CollaborationResponse;
import com.api.musiconnect.dto.response.UserResponse;
import com.api.musiconnect.exception.BadRequestException;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.mapper.CollaborationMapper;
import com.api.musiconnect.mapper.UserMapper;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.CollaborationStatus;
import com.api.musiconnect.model.entity.Collaboration;
import com.api.musiconnect.repository.CollaborationRepository;
import com.api.musiconnect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final UserRepository userRepository;

    @Transactional
    public CollaborationResponse crearColaboracion(CollaborationRequest request) 
    {
        if (request.fechaInicio().isAfter(request.fechaFin())) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        if (collaborationRepository.existsByTituloIgnoreCase(request.titulo())) {
            throw new BusinessRuleException("Ya existe una colaboración con este título.");
        }

        User usuario = userRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        // Validar disponibilidad del usuario
        if (!Boolean.TRUE.equals(usuario.getDisponibilidad())) {
            throw new BusinessRuleException("No puedes crear una colaboracion mientras estés como no disponible.");
        }

        Collaboration collaboration = CollaborationMapper.toEntity(request, usuario);
        return CollaborationMapper.toResponse(collaborationRepository.save(collaboration));
    }
    
    @Transactional
    public Map<String, String> updateCollaboration(Long id, CollaborationUpdateRequest request) {
        Collaboration collaboration = collaborationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaboración no encontrada."));

        if (!collaboration.getUsuario().getUserId().equals(request.usuarioId())) {
            throw new BusinessRuleException("No tiene permisos para editar esta colaboración.");
        }

        if (request.fechaInicio().isAfter(request.fechaFin())) {
            throw new BusinessRuleException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        if (request.estado() == null || !EnumSet.of(
                CollaborationStatus.PENDIENTE,
                CollaborationStatus.EN_PROGRESO,
                CollaborationStatus.FINALIZADO
            ).contains(request.estado())) {
            throw new BusinessRuleException("Estado no válido.");
        }

        collaboration.setTitulo(request.titulo());
        collaboration.setDescripcion(request.descripcion());
        collaboration.setFechaInicio(request.fechaInicio());
        collaboration.setFechaFin(request.fechaFin());
        collaboration.setEstado(request.estado());

        collaborationRepository.save(collaboration);

        return Map.of("message", "Colaboración actualizada correctamente.");
    }

    @Transactional
    public List<CollaborationResponse> listarColaboracionesActivas() {
        List<Collaboration> colaboraciones = collaborationRepository.findByEstadoIn(
            List.of(CollaborationStatus.PENDIENTE, CollaborationStatus.EN_PROGRESO)
        );

        if (colaboraciones.isEmpty()) {
            throw new BusinessRuleException("No hay colaboraciones activas actualmente.");
        }

        return colaboraciones.stream()
            .map(CollaborationMapper::toResponse)
            .toList();
    }

    public List<CollaborationResponse> getAllCollaborations() {
        List<Collaboration> colaboraciones = collaborationRepository.findAll();
        return colaboraciones.stream()
                .map(CollaborationMapper::toResponse)
                .toList();
    }

    public List<CollaborationResponse> getByNombreArtistico(String nombreArtistico) {
        if (nombreArtistico == null || nombreArtistico.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre artístico es obligatorio.");
        }

        // en esta parte se ignora mayúsculas/minúsculas y trim
        String clean = nombreArtistico.trim();
        List<Collaboration> colaboraciones = collaborationRepository.findByUsuario_NombreArtisticoIgnoreCase(clean);

        return colaboraciones.stream()
                .map(CollaborationMapper::toResponse)
                .toList();
    }

    public Map<String, String> addColaborador(Long collaborationId, String nombreArtistico) {
        Collaboration colaboracion = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Colaboración no encontrada."));

        if (nombreArtistico == null || nombreArtistico.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un nombre artístico válido.");
        }

        User user = userRepository.findByNombreArtisticoIgnoreCase(nombreArtistico.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ese nombre artístico no encontrado."));

        if (!Boolean.TRUE.equals(user.getDisponibilidad())) {
            throw new BusinessRuleException("Este usuario no está disponible para colaborar.");
        }

        if (colaboracion.getUsuario().getUserId().equals(user.getUserId())) {
            throw new BusinessRuleException("El creador no puede agregarse como colaborador.");
        }

        if (colaboracion.getColaboradores().contains(user)) {
            throw new BusinessRuleException("El usuario ya forma parte de la colaboración.");
        }

        colaboracion.getColaboradores().add(user);
        collaborationRepository.save(colaboracion);

        return Map.of("message", "Colaborador añadido correctamente");
    }

    public Map<String, String> deleteCollaboration(Long collaborationId, Long userId) {
        if (collaborationId == null || userId == null) {
            throw new IllegalArgumentException("ID de colaboración y usuario requeridos.");
        }

        Collaboration colaboracion = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Colaboración no encontrada."));

        if (!colaboracion.getUsuario().getUserId().equals(userId)) {
            throw new BusinessRuleException("Solo el creador puede eliminar esta colaboración.");
        }

        collaborationRepository.deleteById(collaborationId);
        return Map.of("message", "Colaboración eliminada correctamente");
    }

    public CollaborationResponse getById(Long id) {
        Collaboration c = collaborationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Colaboración no encontrada."));
        return CollaborationMapper.toResponse(c);
    }

    public List<UserResponse> listarColaboradores(Long id) {
        Collaboration colaboracion = collaborationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaboración no encontrada."));

        return colaboracion.getColaboradores().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public List<String> listarEstadosColaboracion() {
        return Arrays.stream(CollaborationStatus.values())
            .map(Enum::name)
            .toList();
    }
}
