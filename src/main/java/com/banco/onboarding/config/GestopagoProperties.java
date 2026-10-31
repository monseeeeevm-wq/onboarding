package com.banco.onboarding.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gestopago")
public record GestopagoProperties(
        @NotBlank String baseUrl,
        @NotBlank String authenticatePath,
        @NotBlank String productListPath,
        @NotBlank String sendTxPath,
        @NotBlank String idDistribuidor,
        @NotBlank String codigoDispositivo,
        @NotBlank String password,
        @Positive int connectTimeoutMs,
        @Positive int readTimeoutMs
) {

    /** Evita que la contraseña aparezca en logs si alguien imprime el objeto. */
    @Override
    public String toString() {
        return "GestopagoProperties[baseUrl=" + baseUrl
                + ", idDistribuidor=" + idDistribuidor
                + ", password=****]";
    }
}