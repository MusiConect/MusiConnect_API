package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ConvocationUpdateRequest(

    @NotNull(message = "Debe proporcionar el ID del usuario que solicita la edición")
    Long usuarioId,

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 4, max = 50, message = "El título debe tener entre 4 y 50 caracteres.")
    String titulo,

    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres.")
    String descripcion,

    @NotNull(message = "Debe indicar la fecha límite")
    LocalDate fechaLimite
) {}
