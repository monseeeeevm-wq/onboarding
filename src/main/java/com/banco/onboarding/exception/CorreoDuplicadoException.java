package com.banco.onboarding.exception;

public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String correo) {
        super("El correo " + correo + " ya está registrado");
    }
}