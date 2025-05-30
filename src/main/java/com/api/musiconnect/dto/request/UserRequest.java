package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record UserRequest(
    @Email @NotBlank String email,
    @NotBlank @Size(min = 6) String password,
    @NotBlank @Size(min = 4, max = 50) String nombreArtistico,
    String instrumentos,
    @Size(max = 300) String bio,
    @NotBlank String ubicacion,
    Boolean disponibilidad,
    @NotNull Long roleId,
    List<String> generosMusicales // nombres del enum como string (EJEMPLO: "ROCK", "JAZZ")
) {}
