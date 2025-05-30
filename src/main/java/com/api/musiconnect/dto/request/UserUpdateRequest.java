package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record UserUpdateRequest(

    @Email(message = "El correo debe ser válido")
    String email,

    String bio,

    String ubicacion,

    String instrumentos,

    List<String> generos,

    Boolean disponibilidad
) {}
