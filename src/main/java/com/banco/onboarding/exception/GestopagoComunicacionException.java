package com.banco.onboarding.exception;

public class GestopagoComunicacionException extends GestopagoException {
    public GestopagoComunicacionException(String operacion) {
        super("No fue posible comunicarse con el proveedor al ejecutar la operación " + operacion);
    }
}