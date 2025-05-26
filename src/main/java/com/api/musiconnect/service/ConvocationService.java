package com.api.musiconnect.service;

import com.api.musiconnect.dto.request.ConvocationRequest;
import com.api.musiconnect.dto.request.ConvocationUpdateRequest;
import com.api.musiconnect.dto.request.FavoriteConvocationRequest;
import com.api.musiconnect.dto.response.ConvocationResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.mapper.ConvocationMapper;
import com.api.musiconnect.model.entity.Convocation;
import com.api.musiconnect.model.entity.ConvocationFavorite;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.repository.ConvocationFavoriteRepository;
import com.api.musiconnect.repository.ConvocationRepository;
import com.api.musiconnect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConvocationService {

    private final ConvocationRepository convocationRepository;
    private final UserRepository userRepository;
    private final ConvocationFavoriteRepository convocationFavoriteRepository;

    @Transactional
    public ConvocationResponse crearConvocatoria(ConvocationRequest request) {
        User usuario = userRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (request.fechaLimite().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("La fecha límite debe ser posterior a hoy.");
        }

        Convocation convocatoria = ConvocationMapper.toEntity(request, usuario);
        return ConvocationMapper.toResponse(convocationRepository.save(convocatoria));
    }

    @Transactional
    public Map<String, String> editarConvocatoria(Long convocationId, ConvocationUpdateRequest request) {
        Convocation convocatoria = convocationRepository.findById(convocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Convocatoria no encontrada."));

        if (!convocatoria.getUsuario().getUserId().equals(request.usuarioId())) {
            throw new BusinessRuleException("No tiene permisos para editar esta convocatoria.");
        }

        if (convocatoria.getFechaLimite().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("No es posible editar una convocatoria finalizada.");
        }

        convocatoria.setTitulo(request.titulo());
        convocatoria.setDescripcion(request.descripcion());
        convocatoria.setFechaLimite(request.fechaLimite());

        convocationRepository.save(convocatoria);

        return Map.of("message", "Convocatoria actualizada correctamente.");
    }

    @Transactional
    public List<ConvocationResponse> listarConvocatoriasActivas() {
        List<Convocation> convocatorias = convocationRepository.findAll()
                .stream()
                .filter(Convocation::getActiva)
                .toList();

        if (convocatorias.isEmpty()) {
            throw new BusinessRuleException("Actualmente no hay convocatorias disponibles.");
        }

        return convocatorias.stream()
                .map(ConvocationMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<ConvocationResponse> listarConvocatoriasFavoritasPorUsuario(Long userId) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        List<ConvocationFavorite> favoritos = convocationFavoriteRepository.findByUsuarioUserId(userId);

        List<Convocation> convocatoriasActivas = favoritos.stream()
                .map(ConvocationFavorite::getConvocatoria)
                .filter(Convocation::getActiva)
                .toList();

        if (convocatoriasActivas.isEmpty()) {
            throw new BusinessRuleException("No tienes convocatorias favoritas activas.");
        }

        return convocatoriasActivas.stream()
                .map(ConvocationMapper::toResponse)
                .toList();
    }

    @Transactional
    public Map<String, String> marcarComoFavorita(FavoriteConvocationRequest request) {
        User usuario = userRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Convocation convocatoria = convocationRepository.findById(request.convocatoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Convocatoria no encontrada."));

        if (!convocatoria.getActiva()) {
            throw new BusinessRuleException("No se puede marcar una convocatoria inactiva como favorita.");
        }

        if (convocationFavoriteRepository.existsByUsuarioAndConvocatoria(usuario, convocatoria)) {
            throw new BusinessRuleException("Esta convocatoria ya está marcada como favorita.");
        }

        ConvocationFavorite favorita = ConvocationFavorite.builder()
                .usuario(usuario)
                .convocatoria(convocatoria)
                .build();

        convocationFavoriteRepository.save(favorita);

        return Map.of("message", "Convocatoria marcada como favorita.");
    }

    @Transactional
    public Map<String, String> eliminarDeFavoritas(FavoriteConvocationRequest request) {
        User usuario = userRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Convocation convocatoria = convocationRepository.findById(request.convocatoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Convocatoria no encontrada."));

        ConvocationFavorite favorita = convocationFavoriteRepository
                .findByUsuarioAndConvocatoria(usuario, convocatoria)
                .orElseThrow(() -> new BusinessRuleException("Esta convocatoria no está marcada como favorita."));

        convocationFavoriteRepository.delete(favorita);

        return Map.of("message", "Convocatoria removida de favoritas.");
    }
}
