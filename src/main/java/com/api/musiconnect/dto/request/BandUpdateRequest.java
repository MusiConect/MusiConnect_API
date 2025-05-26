package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BandUpdateRequest(
    @NotBlank(message = "El nombre de la banda es obligatorio")
    @Size(min = 4, max = 50, message = "El nombre debe tener entre 4 y 50 caracteres.")
    String nombre,

    String descripcion,

    @NotNull(message = "Debe proporcionar los géneros musicales")
    List<String> generos,

    @NotNull(message = "Debe proporcionar el ID del administrador")
    Long adminId
) {}
