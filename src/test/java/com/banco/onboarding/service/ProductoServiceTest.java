package com.banco.onboarding.service;

import com.banco.onboarding.dto.ProductoResponse;
import com.banco.onboarding.exception.GestopagoAutenticacionException;
import com.banco.onboarding.exception.GestopagoComunicacionException;
import com.banco.onboarding.exception.GestopagoRespuestaException;
import com.banco.onboarding.exception.GestopagoTimeoutException;
import com.banco.onboarding.integration.gestopago.GestopagoClient;
import com.banco.onboarding.integration.gestopago.ProductoGestopago;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private GestopagoClient gestopagoClient;

    @InjectMocks
    private ProductoService service;

    private ProductoGestopago producto(int idProducto, String precio) {
        return new ProductoGestopago(56, "AGUAKAN (Cancun)", idProducto, "Agua Cancun", 15,
                new BigDecimal(precio), "c", false, "Atención a clientes 073");
    }

    @Test
    void listarProductos_conCatalogo_devuelveLosProductosSinCamposInternos() {
        when(gestopagoClient.obtenerCatalogo()).thenReturn(List.of(producto(185, "10.0"), producto(656, "25.5")));

        List<ProductoResponse> resultado = service.listarProductos();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).idProducto()).isEqualTo(185);
        assertThat(resultado.get(0).servicio()).isEqualTo("AGUAKAN (Cancun)");
        assertThat(resultado.get(1).precio()).isEqualByComparingTo("25.5");
    }

    @Test
    void listarProductos_catalogoVacio_devuelveListaVacia() {
        when(gestopagoClient.obtenerCatalogo()).thenReturn(List.of());

        assertThat(service.listarProductos()).isEmpty();
    }

    @Test
    void listarProductos_timeoutDelProveedor_propagaLaExcepcion() {
        when(gestopagoClient.obtenerCatalogo()).thenThrow(new GestopagoTimeoutException("getProductList"));

        assertThrows(GestopagoTimeoutException.class, () -> service.listarProductos());
    }

    @Test
    void listarProductos_errorDeAutenticacion_propagaLaExcepcion() {
        when(gestopagoClient.obtenerCatalogo()).thenThrow(new GestopagoAutenticacionException());

        assertThrows(GestopagoAutenticacionException.class, () -> service.listarProductos());
    }

    @Test
    void listarProductos_errorDeComunicacion_propagaLaExcepcion() {
        when(gestopagoClient.obtenerCatalogo()).thenThrow(new GestopagoComunicacionException("getProductList"));

        assertThrows(GestopagoComunicacionException.class, () -> service.listarProductos());
    }

    @Test
    void listarProductos_respuestaNoExitosa_propagaLaExcepcionConElCodigoDelProveedor() {
        when(gestopagoClient.obtenerCatalogo())
                .thenThrow(new GestopagoRespuestaException("El proveedor respondió con el código 05", "05"));

        GestopagoRespuestaException error =
                assertThrows(GestopagoRespuestaException.class, () -> service.listarProductos());

        assertThat(error.getCodigoProveedor()).isEqualTo("05");
    }
}