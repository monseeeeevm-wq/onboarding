package com.banco.onboarding.exception;

public abstract class GestopagoException extends RuntimeException {
    protected GestopagoException(String mensaje) {
        super(mensaje);
    }
}