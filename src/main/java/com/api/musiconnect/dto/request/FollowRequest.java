package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.NotNull;

public record FollowRequest(

    @NotNull(message = "Debe especificar el ID del usuario que realiza el seguimiento")
    Long followerId,

    Long followedUserId,

    Long followedBandId
) {}
