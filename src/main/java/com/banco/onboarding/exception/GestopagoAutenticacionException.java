package com.banco.onboarding.exception;

public class GestopagoAutenticacionException extends GestopagoException {
    public GestopagoAutenticacionException() {
        super("El proveedor rechazó las credenciales de la integración");
    }
}