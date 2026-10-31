package com.banco.onboarding.integration.gestopago;

import com.banco.onboarding.config.GestopagoProperties;
import com.banco.onboarding.exception.GestopagoAutenticacionException;
import com.banco.onboarding.exception.GestopagoComunicacionException;
import com.banco.onboarding.exception.GestopagoException;
import com.banco.onboarding.exception.GestopagoRespuestaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GestopagoHttpClient implements GestopagoClient {

    private static final String OPERACION = "getProductList";

    private final RestClient restClient;
    private final GestopagoProperties properties;
    private final GestopagoTokenProvider tokenProvider;
    private final GestopagoXmlParser parser;

    @Override
    public List<ProductoGestopago> obtenerCatalogo() {
        long inicio = System.nanoTime();
        log.info("Gestopago {}: inicio de la invocación", OPERACION);
        try {
            List<ProductoGestopago> productos = parser.parsearCatalogo(consultarXml());
            log.info("Gestopago {}: fin exitoso, {} productos en {} ms",
                    OPERACION, productos.size(), milisDesde(inicio));
            return productos;
        } catch (GestopagoException e) {
            log.error("Gestopago {}: fin con error tras {} ms ({})",
                    OPERACION, milisDesde(inicio), e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Si el token fue rechazado se renueva y se reintenta UNA sola vez. Es seguro aquí porque
     * es una consulta de solo lectura; en un pago (sendTx) NO se reintenta automáticamente.
     */
    private String consultarXml() {
        String token = tokenProvider.obtenerToken();
        try {
            return solicitar(token);
        } catch (GestopagoAutenticacionException e) {
            log.warn("Gestopago {}: token rechazado, se renueva y se reintenta una vez", OPERACION);
            tokenProvider.invalidar(token);
            return solicitar(tokenProvider.obtenerToken());
        }
    }

    private String solicitar(String token) {
        try {
            byte[] cuerpo = restClient.get()
                    .uri(properties.productListPath())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(byte[].class);
            return cuerpo == null ? "" : new String(cuerpo, StandardCharsets.UTF_8);
        } catch (RestClientResponseException e) {
            throw traducir(e);
        } catch (ResourceAccessException e) {
            throw GestopagoErrores.deRed(e, OPERACION);
        } catch (RestClientException e) {
            throw new GestopagoComunicacionException(OPERACION);
        }
    }

    private GestopagoException traducir(RestClientResponseException error) {
        int estado = error.getStatusCode().value();
        if (estado == 401 || estado == 403) {
            return new GestopagoAutenticacionException();
        }
        return new GestopagoRespuestaException("El proveedor respondió con HTTP " + estado, "HTTP_" + estado);
    }

    private long milisDesde(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }
}