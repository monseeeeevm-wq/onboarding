package com.banco.onboarding.dto;

import com.banco.onboarding.integration.gestopago.ProductoGestopago;

import java.math.BigDecimal;

public record ProductoResponse(
        Integer idServicio,
        String servicio,
        Integer idProducto,
        String producto,
        BigDecimal precio,
        String tipoReferencia,
        String leyenda
) {
    public static ProductoResponse desde(ProductoGestopago p) {
        return new ProductoResponse(p.idServicio(), p.servicio(), p.idProducto(), p.producto(),
                p.precio(), p.tipoReferencia(), p.leyenda());
    }
}