package com.api.musiconnect.dto.response;

import com.api.musiconnect.model.enums.CollaborationStatus;
import java.time.LocalDate;
import java.util.List;

public record CollaborationResponse(
    Long colaboracionId,
    String titulo,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    CollaborationStatus estado,
    String nombreUsuario,
    List<String> colaboradores
) {}
