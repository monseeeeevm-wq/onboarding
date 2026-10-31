package com.banco.onboarding.service;

import com.banco.onboarding.dto.ProductoResponse;
import com.banco.onboarding.integration.gestopago.GestopagoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final GestopagoClient gestopagoClient;

    public List<ProductoResponse> listarProductos() {
        return gestopagoClient.obtenerCatalogo().stream()
                .map(ProductoResponse::desde)
                .toList();
    }
}