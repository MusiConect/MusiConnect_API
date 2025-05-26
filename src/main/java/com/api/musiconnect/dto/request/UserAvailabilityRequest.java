package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserAvailabilityRequest(

    @NotNull(message = "Debe proporcionar el estado de disponibilidad.")
    Boolean disponibilidad
) {}
