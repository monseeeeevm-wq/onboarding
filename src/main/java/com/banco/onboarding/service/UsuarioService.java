package com.banco.onboarding.service;

import com.banco.onboarding.dto.*;
import com.banco.onboarding.entity.Usuario;
import com.banco.onboarding.exception.CorreoDuplicadoException;
import com.banco.onboarding.exception.ReglaNegocioException;
import com.banco.onboarding.exception.UsuarioNoEncontradoException;
import com.banco.onboarding.repository.UsuarioRepository;
import com.banco.onboarding.repository.UsuarioSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final int EDAD_MINIMA = 18;
    private static final int PAGINA_POR_DEFECTO = 0;
    private static final int TAMANIO_POR_DEFECTO = 10;

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        String correo = normalizarCorreo(request.correo());
        validarMayorDeEdad(request.fechaNacimiento());
        if (repository.existsByCorreoIgnoreCase(correo)) {
            throw new CorreoDuplicadoException(correo);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(request.telefono());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setRol(request.rol());
        usuario.setPassword(passwordEncoder.encode(request.password()));

        return UsuarioResponse.desde(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id) {
        return UsuarioResponse.desde(buscarEntidad(id));
    }

    @Transactional
    public UsuarioResponse actualizar(UsuarioActualizarRequest request) {
        Usuario usuario = buscarEntidad(request.id());
        String correo = normalizarCorreo(request.correo());
        validarMayorDeEdad(request.fechaNacimiento());
        if (repository.existsByCorreoIgnoreCaseAndIdNot(correo, usuario.getId())) {
            throw new CorreoDuplicadoException(correo);
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(request.telefono());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setRol(request.rol());

        return UsuarioResponse.desde(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponse darDeBaja(Integer id) {
        Usuario usuario = buscarEntidad(id);
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new ReglaNegocioException("El usuario con id " + id + " ya está dado de baja");
        }
        usuario.setActivo(false);
        return UsuarioResponse.desde(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioResponse> buscar(UsuarioBusquedaRequest filtros) {
        if (filtros.fechaNacimientoDesde() != null && filtros.fechaNacimientoHasta() != null
                && filtros.fechaNacimientoDesde().isAfter(filtros.fechaNacimientoHasta())) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la fecha final");
        }

        int pagina = filtros.pagina() != null ? filtros.pagina() : PAGINA_POR_DEFECTO;
        int tamanio = filtros.tamanio() != null ? filtros.tamanio() : TAMANIO_POR_DEFECTO;
        String ordenarPor = filtros.ordenarPor() != null ? filtros.ordenarPor() : "id";
        Sort.Direction direccion = "DESC".equalsIgnoreCase(filtros.direccion())
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by(direccion, ordenarPor));
        Page<Usuario> resultado = repository.findAll(UsuarioSpecifications.desde(filtros), pageable);

        return PaginaResponse.desde(resultado.map(UsuarioResponse::desde));
    }

    private Usuario buscarEntidad(Integer id) {
        return repository.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private void validarMayorDeEdad(LocalDate fechaNacimiento) {
        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < EDAD_MINIMA) {
            throw new ReglaNegocioException("El usuario debe ser mayor de edad (18 años o más)");
        }
    }
}