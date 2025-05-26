package com.api.musiconnect.dto.response;

import java.time.LocalDateTime;

public record FollowResponse(
    Long followId,
    String followerNombre,
    String seguidoNombre,
    String tipoSeguido,
    LocalDateTime fechaSeguimiento,
    String message
) {}
