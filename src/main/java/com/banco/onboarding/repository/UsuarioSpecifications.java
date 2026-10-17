package com.banco.onboarding.repository;

import com.banco.onboarding.dto.UsuarioBusquedaRequest;
import com.banco.onboarding.entity.Usuario;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    public static Specification<Usuario> desde(UsuarioBusquedaRequest f) {
        return (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();

            if (tieneTexto(f.nombre())) {
                String patron = "%" + f.nombre().trim().toLowerCase() + "%";
                filtros.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), patron),
                        cb.like(cb.lower(root.get("apellidos")), patron)));
            }
            if (tieneTexto(f.correo())) {
                filtros.add(cb.like(cb.lower(root.get("correo")),
                        "%" + f.correo().trim().toLowerCase() + "%"));
            }
            if (f.rol() != null) {
                filtros.add(cb.equal(root.get("rol"), f.rol()));
            }
            if (f.activo() != null) {
                filtros.add(cb.equal(root.get("activo"), f.activo()));
            }
            if (f.fechaNacimientoDesde() != null) {
                filtros.add(cb.greaterThanOrEqualTo(root.get("fechaNacimiento"), f.fechaNacimientoDesde()));
            }
            if (f.fechaNacimientoHasta() != null) {
                filtros.add(cb.lessThanOrEqualTo(root.get("fechaNacimiento"), f.fechaNacimientoHasta()));
            }
            return cb.and(filtros.toArray(new Predicate[0]));
        };
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}