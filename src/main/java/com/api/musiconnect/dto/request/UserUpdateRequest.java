package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record UserUpdateRequest(

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo debe ser válido")
    String email,

    String bio,

    String instrumentos,

    @NotNull(message = "Debe especificar los géneros musicales")
    List<String> generos,

    @NotNull(message = "Debe indicar si está disponible")
    Boolean disponibilidad
) {}
