package com.api.musiconnect.dto.response;

import com.api.musiconnect.model.enums.CollaborationStatus;
import java.time.LocalDate;

public record CollaborationResponse(
    Long colaboracionId,
    String titulo,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    CollaborationStatus estado,
    String nombreUsuario,
    String nombreBanda
) {}