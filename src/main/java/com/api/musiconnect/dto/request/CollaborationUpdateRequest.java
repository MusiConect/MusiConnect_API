package com.api.musiconnect.dto.request;

import com.api.musiconnect.model.enums.CollaborationStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CollaborationUpdateRequest(

    @NotNull(message = "Debe especificar el ID del usuario que realiza la acción")
    Long usuarioId,

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 4, max = 50, message = "El título debe tener entre 4 y 50 caracteres.")
    String titulo,

    String descripcion,

    @NotNull(message = "Debe proporcionar la fecha de inicio")
    LocalDate fechaInicio,

    @NotNull(message = "Debe proporcionar la fecha de fin")
    LocalDate fechaFin,

    @NotNull(message = "Debe proporcionar un estado válido")
    CollaborationStatus estado
) {}
