package com.banco.onboarding.exception;

public class GestopagoRespuestaException extends GestopagoException {

    private final String codigoProveedor;

    public GestopagoRespuestaException(String mensaje, String codigoProveedor) {
        super(mensaje);
        this.codigoProveedor = codigoProveedor;
    }

    public String getCodigoProveedor() {
        return codigoProveedor;
    }
}