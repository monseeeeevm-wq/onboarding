package com.banco.onboarding.integration.gestopago;

import java.math.BigDecimal;

public record ProductoGestopago(
        Integer idServicio,
        String servicio,
        Integer idProducto,
        String producto,
        Integer idCategoria,
        BigDecimal precio,
        String tipoReferencia,
        boolean requiereDigitoVerificador,
        String leyenda
) {
}