package com.banco.onboarding.controller;

import com.banco.onboarding.dto.*;
import com.banco.onboarding.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        UsuarioResponse usuario = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(HttpStatus.CREATED, "Usuario creado correctamente", usuario));
    }

    @PostMapping("/consultar")
    public ResponseEntity<ApiResponse<UsuarioResponse>> consultar(@Valid @RequestBody UsuarioIdRequest request) {
        UsuarioResponse usuario = service.obtener(request.id());
        return ResponseEntity.ok(ApiResponse.ok(HttpStatus.OK, "Usuario encontrado", usuario));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(@Valid @RequestBody UsuarioActualizarRequest request) {
        UsuarioResponse usuario = service.actualizar(request);
        return ResponseEntity.ok(ApiResponse.ok(HttpStatus.OK, "Usuario actualizado correctamente", usuario));
    }

    @PatchMapping("/baja")
    public ResponseEntity<ApiResponse<UsuarioResponse>> darDeBaja(@Valid @RequestBody UsuarioIdRequest request) {
        UsuarioResponse usuario = service.darDeBaja(request.id());
        return ResponseEntity.ok(ApiResponse.ok(HttpStatus.OK, "Usuario dado de baja correctamente", usuario));
    }

    @PostMapping("/buscar")
    public ResponseEntity<ApiResponse<PaginaResponse<UsuarioResponse>>> buscar(
            @Valid @RequestBody UsuarioBusquedaRequest request) {
        PaginaResponse<UsuarioResponse> pagina = service.buscar(request);
        return ResponseEntity.ok(ApiResponse.ok(HttpStatus.OK, "Búsqueda realizada correctamente", pagina));
    }
}