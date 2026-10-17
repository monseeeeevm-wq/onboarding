package com.banco.onboarding.dto;

import com.banco.onboarding.entity.Rol;
import com.banco.onboarding.entity.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String apellidos,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        Rol rol,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getTelefono(), u.getFechaNacimiento(), u.getRol(), u.getActivo(),
                u.getFechaCreacion(), u.getFechaActualizacion());
    }
}