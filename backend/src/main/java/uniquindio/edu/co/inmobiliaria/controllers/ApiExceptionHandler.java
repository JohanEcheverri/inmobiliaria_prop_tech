package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uniquindio.edu.co.inmobiliaria.models.dto.ApiError;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
/**
 * Interceptor global de excepciones para la capa de controladores REST.
 * Captura excepciones específicas arrojadas por la lógica de negocio y las
 * traduce en respuestas HTTP estructuradas (ej. 400 Bad Request) con un formato estándar.
 */
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Mapea IllegalArgumentException a una respuesta HTTP 400 con un cuerpo ApiError.
     * Centraliza el manejo de argumentos inválidos para la API.
     *
     * @param exception excepción capturada
     * @return ResponseEntity con ApiError y código 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> manejarIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(exception.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarExceptionGenerica(Exception exception) {
        log.error("Error no controlado en la API", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        exception.getMessage() != null ? exception.getMessage() : "Error interno del servidor",
                        LocalDateTime.now()));
    }
}
