package com.banco.onboarding.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UsuarioIdRequest(

        @NotNull(message = "El id es obligatorio")
        @Positive(message = "El id debe ser mayor a cero")
        Integer id
) {
}