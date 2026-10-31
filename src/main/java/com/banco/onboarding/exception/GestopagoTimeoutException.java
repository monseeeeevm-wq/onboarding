package com.banco.onboarding.exception;

public class GestopagoTimeoutException extends GestopagoException {
    public GestopagoTimeoutException(String operacion) {
        super("Tiempo de espera agotado al ejecutar la operación " + operacion);
    }
}