package com.banco.onboarding.dto;

import com.banco.onboarding.entity.Rol;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UsuarioBusquedaRequest(

        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
        String correo,

        Rol rol,

        Boolean activo,

        @Past(message = "La fecha inicial debe ser una fecha pasada")
        LocalDate fechaNacimientoDesde,

        @Past(message = "La fecha final debe ser una fecha pasada")
        LocalDate fechaNacimientoHasta,

        @Min(value = 0, message = "La página no puede ser negativa")
        Integer pagina,

        @Min(value = 1, message = "El tamaño mínimo es 1")
        @Max(value = 100, message = "El tamaño máximo es 100")
        Integer tamanio,

        @Pattern(regexp = "^(id|nombre|apellidos|correo|fechaNacimiento|fechaCreacion)$",
                message = "Solo se puede ordenar por: id, nombre, apellidos, correo, fechaNacimiento o fechaCreacion")
        String ordenarPor,

        @Pattern(regexp = "^(?i)(ASC|DESC)$", message = "La dirección debe ser ASC o DESC")
        String direccion
) {
}