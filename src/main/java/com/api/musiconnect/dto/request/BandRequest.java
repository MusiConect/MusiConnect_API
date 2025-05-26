package com.api.musiconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BandRequest(

    @NotBlank(message = "El nombre de la banda es obligatorio")
    @Size(min = 4, max = 50, message = "El nombre de la banda debe tener entre 4 y 50 caracteres.")
    String nombre,

    String descripcion,

    List<String> generos,

    Long adminId
) {}
