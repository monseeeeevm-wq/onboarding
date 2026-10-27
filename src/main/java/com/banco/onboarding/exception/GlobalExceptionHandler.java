package com.banco.onboarding.exception;

import com.banco.onboarding.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Error de validación en los datos enviados", errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> manejarCuerpoInvalido(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es inválido",
                List.of("Revisa que el JSON esté completo y bien formado, que las fechas usen el formato yyyy-MM-dd y que el rol sea ADMIN o USUARIO"));
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> manejarNoEncontrado(UsuarioNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, "Usuario no encontrado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiResponse<Void>> manejarCorreoDuplicado(CorreoDuplicadoException ex) {
        return construir(HttpStatus.CONFLICT, "Correo duplicado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiResponse<Void>> manejarReglaNegocio(ReglaNegocioException ex) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, "Los datos no cumplen las reglas del sistema",
                List.of(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> manejarIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return construir(HttpStatus.CONFLICT, "Conflicto con datos existentes",
                List.of("La operación entra en conflicto con información ya registrada"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> manejarMetodo(HttpRequestMethodNotSupportedException ex) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido",
                List.of("El método " + ex.getMethod() + " no está permitido en este endpoint"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> manejarTipoContenido(HttpMediaTypeNotSupportedException ex) {
        return construir(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado",
                List.of("Envía el cuerpo como application/json"));
    }

    @ExceptionHandler(GestopagoAutenticacionException.class)
    public ResponseEntity<ApiResponse<Void>> manejarGestopagoAutenticacion(GestopagoAutenticacionException ex) {
        log.error("Fallo de autenticación con Gestopago: {}", ex.getMessage());
        return construir(HttpStatus.BAD_GATEWAY, "Servicio de pagos no disponible",
                List.of("No fue posible autenticar con el proveedor. Intenta más tarde"));
    }

    @ExceptionHandler(GestopagoTimeoutException.class)
    public ResponseEntity<ApiResponse<Void>> manejarGestopagoTimeout(GestopagoTimeoutException ex) {
        log.warn("Timeout con Gestopago: {}", ex.getMessage());
        return construir(HttpStatus.GATEWAY_TIMEOUT, "El proveedor tardó demasiado en responder",
                List.of("Intenta de nuevo en unos momentos"));
    }

    @ExceptionHandler(GestopagoComunicacionException.class)
    public ResponseEntity<ApiResponse<Void>> manejarGestopagoComunicacion(GestopagoComunicacionException ex) {
        log.error("Error de comunicación con Gestopago: {}", ex.getMessage());
        return construir(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de pagos no disponible",
                List.of("No fue posible comunicarse con el proveedor. Intenta más tarde"));
    }

    @ExceptionHandler(GestopagoRespuestaException.class)
    public ResponseEntity<ApiResponse<Void>> manejarGestopagoRespuesta(GestopagoRespuestaException ex) {
        log.error("Respuesta no exitosa de Gestopago (código {}): {}", ex.getCodigoProveedor(), ex.getMessage());
        return construir(HttpStatus.BAD_GATEWAY, "El proveedor devolvió una respuesta no exitosa",
                List.of("Código del proveedor: " + ex.getCodigoProveedor()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> manejarInesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
                List.of("Ocurrió un error inesperado. Intenta de nuevo más tarde"));
    }

    private ResponseEntity<ApiResponse<Void>> construir(HttpStatus estado, String mensaje, List<String> errores) {
        return ResponseEntity.status(estado).body(ApiResponse.error(estado, mensaje, errores));
    }
}