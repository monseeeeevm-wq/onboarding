package com.banco.onboarding.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiResponse<T>(
        boolean exito,
        int estado,
        String mensaje,
        T datos,
        List<String> errores,
        LocalDateTime fecha
) {
    public static <T> ApiResponse<T> ok(HttpStatus estado, String mensaje, T datos) {
        return new ApiResponse<>(true, estado.value(), mensaje, datos, List.of(), LocalDateTime.now());
    }

    public static ApiResponse<Void> error(HttpStatus estado, String mensaje, List<String> errores) {
        return new ApiResponse<>(false, estado.value(), mensaje, null, errores, LocalDateTime.now());
    }
}