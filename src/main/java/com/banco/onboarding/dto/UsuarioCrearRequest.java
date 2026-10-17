package com.banco.onboarding.dto;

import com.banco.onboarding.entity.Rol;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UsuarioCrearRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]+$", message = "El nombre solo puede contener letras y espacios")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]+$", message = "Los apellidos solo pueden contener letras y espacios")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos")
        String telefono,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNacimiento,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$",
                message = "La contraseña debe tener entre 8 y 64 caracteres, con mayúscula, minúscula, número y carácter especial")
        String password
) {
}