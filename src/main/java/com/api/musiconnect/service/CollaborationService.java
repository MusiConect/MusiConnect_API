package com.api.musiconnect.service;

import com.api.musiconnect.dto.request.CollaborationRequest;
import com.api.musiconnect.dto.request.CollaborationUpdateRequest;
import com.api.musiconnect.dto.response.CollaborationResponse;
import com.api.musiconnect.exception.BadRequestException;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.mapper.CollaborationMapper;
import com.api.musiconnect.model.entity.Band;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.CollaborationStatus;
import com.api.musiconnect.model.entity.Collaboration;
import com.api.musiconnect.repository.BandRepository;
import com.api.musiconnect.repository.CollaborationRepository;
import com.api.musiconnect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final UserRepository userRepository;
    private final BandRepository bandRepository;

    @Transactional
    public CollaborationResponse crearColaboracion(CollaborationRequest request) 
    {
        if (request.fechaInicio().isAfter(request.fechaFin())) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        User usuario = userRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Band banda = bandRepository.findById(request.bandaId())
                .orElseThrow(() -> new ResourceNotFoundException("Banda no encontrada."));

        Collaboration collaboration = CollaborationMapper.toEntity(request, usuario, banda);
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
}
