package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
    @NotNull(message = "Debe proporcionar el ID del usuario a añadir")
    Long userId,

    @NotNull(message = "Debe proporcionar el ID del administrador que ejecuta la acción")
    Long adminId
) {}
