package com.banco.onboarding.controller;

import com.banco.onboarding.dto.ApiResponse;
import com.banco.onboarding.dto.ProductoResponse;
import com.banco.onboarding.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listar() {
        List<ProductoResponse> productos = service.listarProductos();
        return ResponseEntity.ok(ApiResponse.ok(HttpStatus.OK, "Catálogo de productos obtenido correctamente", productos));
    }
}