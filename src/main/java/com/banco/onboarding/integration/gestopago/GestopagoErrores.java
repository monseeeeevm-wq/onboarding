package com.banco.onboarding.integration.gestopago;

import com.banco.onboarding.exception.GestopagoComunicacionException;
import com.banco.onboarding.exception.GestopagoException;
import com.banco.onboarding.exception.GestopagoTimeoutException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

final class GestopagoErrores {

    private GestopagoErrores() {
    }

    static GestopagoException deRed(ResourceAccessException error, String operacion) {
        return esTimeout(error)
                ? new GestopagoTimeoutException(operacion)
                : new GestopagoComunicacionException(operacion);
    }

    private static boolean esTimeout(Throwable error) {
        Throwable actual = error;
        while (actual != null) {
            if (actual instanceof HttpTimeoutException || actual instanceof SocketTimeoutException) {
                return true;
            }
            actual = actual.getCause() == actual ? null : actual.getCause();
        }
        return false;
    }
}