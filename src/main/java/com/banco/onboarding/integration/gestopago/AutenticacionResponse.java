package com.banco.onboarding.integration.gestopago;

public record AutenticacionResponse(String token, boolean success) {

    @Override
    public String toString() {
        return "AutenticacionResponse[success=" + success + ", token=****]";
    }
}