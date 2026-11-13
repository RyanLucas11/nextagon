
package br.com.nextagon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        return ResponseEntity.badRequest().body(
                buildBody(
                        HttpStatus.BAD_REQUEST,
                        "Dados inválidos",
                        errors
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildBody(
                        HttpStatus.FORBIDDEN,
                        "Você não tem permissão para realizar esta operação",
                        null
                ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex
    ) {
        HttpStatus status = HttpStatus.resolve(
                ex.getStatusCode().value()
        );

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String message = ex.getReason() != null
                ? ex.getReason()
                : status.getReasonPhrase();

        return ResponseEntity.status(status)
                .body(buildBody(status, message, null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex
    ) {
        log.warn(
                "Operação rejeitada por violação de integridade no banco",
                ex
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(
                        HttpStatus.CONFLICT,
                        "A operação não pode ser concluída devido a um conflito de dados",
                        null
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        return ResponseEntity.badRequest()
                .body(buildBody(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(
                        HttpStatus.CONFLICT,
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurity(
            SecurityException ex
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildBody(
                        HttpStatus.FORBIDDEN,
                        "Acesso negado",
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex
    ) {
        log.error("Erro interno não tratado", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno no servidor",
                        null
                ));
    }

    private Map<String, Object> buildBody(
            HttpStatus status,
            String message,
            Object details
    ) {
        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        if (details != null) {
            body.put("details", details);
        }

        return body;
    }
}
