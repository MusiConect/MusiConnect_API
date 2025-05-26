package com.api.musiconnect.service;

import com.api.musiconnect.dto.request.FollowRequest;
import com.api.musiconnect.dto.request.UnfollowRequest;
import com.api.musiconnect.dto.response.FollowResponse;
import com.api.musiconnect.dto.response.FollowedProfileResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.mapper.FollowMapper;
import com.api.musiconnect.model.entity.Band;
import com.api.musiconnect.model.entity.Follow;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.repository.BandRepository;
import com.api.musiconnect.repository.FollowRepository;
import com.api.musiconnect.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final BandRepository bandRepository;

    @Transactional
    public FollowResponse crearFollow(FollowRequest request) {
        User follower = userRepository.findById(request.followerId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario que realiza el seguimiento no encontrado."));

        if ((request.followedUserId() == null && request.followedBandId() == null) ||
            (request.followedUserId() != null && request.followedBandId() != null)) {
            throw new BusinessRuleException("Debe seguir a un usuario o a una banda, pero no ambos.");
        }

        if (request.followedUserId() != null) {
            if (request.followerId().equals(request.followedUserId())) {
                throw new BusinessRuleException("No puedes seguirte a ti mismo.");
            }

            if (followRepository.existsByFollowerUserIdAndFollowedUserUserId(request.followerId(), request.followedUserId())) {
                throw new BusinessRuleException("Ya sigues a este perfil.");
            }

            User seguido = userRepository.findById(request.followedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario a seguir no encontrado."));

            Follow follow = Follow.builder()
                    .follower(follower)
                    .followedUser(seguido)
                    .fechaSeguimiento(LocalDateTime.now())
                    .build();

            return FollowMapper.toResponse(followRepository.save(follow));
        }

        if (followRepository.existsByFollowerUserIdAndFollowedBandBandId(request.followerId(), request.followedBandId())) {
            throw new BusinessRuleException("Ya sigues a este perfil.");
        }

        Band banda = bandRepository.findById(request.followedBandId())
                .orElseThrow(() -> new ResourceNotFoundException("Banda a seguir no encontrada."));

        Follow follow = Follow.builder()
                .follower(follower)
                .followedBand(banda)
                .fechaSeguimiento(LocalDateTime.now())
                .build();

        return FollowMapper.toResponse(followRepository.save(follow));
    }

    @Transactional
    public List<FollowedProfileResponse> listarPerfilesSeguidos(Long userId) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        List<Follow> seguidos = followRepository.findByFollowerUserId(userId);

        if (seguidos.isEmpty()) {
            throw new BusinessRuleException("Aún no sigues a ningún perfil.");
        }

        return seguidos.stream().map(follow -> {
            if (follow.getFollowedUser() != null) {
                User seguido = follow.getFollowedUser();
                return new FollowedProfileResponse(
                    seguido.getUserId(),
                    seguido.getNombreArtistico(),
                    "Usuario",
                    seguido.getDisponibilidad(),
                    seguido.getUbicacion(),
                    null // Aquí puedes mapear imagen si la implementas
                );
            } else {
                Band banda = follow.getFollowedBand();
                return new FollowedProfileResponse(
                    banda.getBandId(),
                    banda.getNombre(),
                    "Banda",
                    null,
                    null,
                    null
                );
            }
        }).toList();
    }

    @Transactional
    public Map<String, String> eliminarFollow(UnfollowRequest request) {
        if ((request.followedUserId() == null && request.followedBandId() == null) ||
            (request.followedUserId() != null && request.followedBandId() != null)) {
            throw new BusinessRuleException("Debe especificar si desea dejar de seguir a un usuario o a una banda, pero no ambos.");
        }

        String nombrePerfil;

        if (request.followedUserId() != null) {
            Follow follow = followRepository.findByFollowerUserIdAndFollowedUserUserId(
                    request.followerId(), request.followedUserId()
            ).orElseThrow(() -> new BusinessRuleException("No sigues a este perfil."));

            nombrePerfil = follow.getFollowedUser().getNombreArtistico();
            followRepository.delete(follow);
        } else {
            Follow follow = followRepository.findByFollowerUserIdAndFollowedBandBandId(
                    request.followerId(), request.followedBandId()
            ).orElseThrow(() -> new BusinessRuleException("No sigues a este perfil."));

            nombrePerfil = follow.getFollowedBand().getNombre();
            followRepository.delete(follow);
        }

        return Map.of("message", "Has dejado de seguir a " + nombrePerfil + ".");
    }
}
