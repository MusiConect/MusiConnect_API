package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.NotNull;

public record UnfollowRequest(

    @NotNull(message = "Debe especificar el ID del usuario que quiere dejar de seguir")
    Long followerId,

    Long followedUserId,

    Long followedBandId
) {}
