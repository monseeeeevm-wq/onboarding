package com.banco.onboarding.integration.gestopago;

import com.banco.onboarding.config.GestopagoProperties;
import com.banco.onboarding.exception.GestopagoAutenticacionException;
import com.banco.onboarding.exception.GestopagoComunicacionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class GestopagoTokenProvider {

    /** El proveedor emite tokens de 24 h; se renueva antes para no usar uno a punto de expirar. */
    private static final Duration VIGENCIA = Duration.ofHours(23);
    private static final String OPERACION = "authenticate";

    private final RestClient restClient;
    private final GestopagoProperties properties;
    private final Clock reloj;

    private final ReentrantLock candado = new ReentrantLock();
    private volatile TokenEnCache actual;

    public String obtenerToken() {
        TokenEnCache token = actual;
        if (vigente(token)) {
            return token.valor();
        }
        candado.lock();
        try {
            if (!vigente(actual)) {
                actual = new TokenEnCache(autenticar(), reloj.instant().plus(VIGENCIA));
            }
            return actual.valor();
        } finally {
            candado.unlock();
        }
    }

    /**
     * Descarta el token solo si es el mismo que el proveedor rechazó; así un hilo lento
     * no borra un token que otro hilo ya renovó.
     */
    public void invalidar(String tokenRechazado) {
        candado.lock();
        try {
            if (actual != null && actual.valor().equals(tokenRechazado)) {
                actual = null;
            }
        } finally {
            candado.unlock();
        }
    }

    private boolean vigente(TokenEnCache token) {
        return token != null && reloj.instant().isBefore(token.expira());
    }

    private String autenticar() {
        log.info("Gestopago {}: solicitando token", OPERACION);
        try {
            AutenticacionResponse respuesta = restClient.post()
                    .uri(uri -> uri.path(properties.authenticatePath())
                            .queryParam("idDistribuidor", "{id}")
                            .queryParam("codigoDispositivo", "{dispositivo}")
                            .queryParam("password", "{password}")
                            .build(properties.idDistribuidor(), properties.codigoDispositivo(),
                                    properties.password()))
                    .retrieve()
                    .body(AutenticacionResponse.class);

            if (respuesta == null || !respuesta.success()
                    || respuesta.token() == null || respuesta.token().isBlank()) {
                throw new GestopagoAutenticacionException();
            }
            return respuesta.token();
        } catch (RestClientResponseException e) {
            log.error("Gestopago {}: el proveedor respondió HTTP {}", OPERACION, e.getStatusCode().value());
            throw new GestopagoAutenticacionException();
        } catch (ResourceAccessException e) {
            throw GestopagoErrores.deRed(e, OPERACION);
        } catch (RestClientException e) {
            throw new GestopagoComunicacionException(OPERACION);
        }
    }

    private record TokenEnCache(String valor, Instant expira) {
        @Override
        public String toString() {
            return "TokenEnCache[expira=" + expira + "]";
        }
    }
}