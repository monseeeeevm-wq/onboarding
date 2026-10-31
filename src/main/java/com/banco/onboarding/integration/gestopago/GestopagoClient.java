package com.banco.onboarding.integration.gestopago;

import java.util.List;

public interface GestopagoClient {

    List<ProductoGestopago> obtenerCatalogo();
}