package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CollaborationRequest(
    @NotBlank @Size(min = 4, max = 50)
    String titulo,
    String descripcion,
    @NotNull
    LocalDate fechaInicio,
    @NotNull
    LocalDate fechaFin,
    @NotNull
    Long usuarioId
) {}