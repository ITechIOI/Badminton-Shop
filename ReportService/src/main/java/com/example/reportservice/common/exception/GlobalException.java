package com.example.reportservice.common.exception;

import com.example.reportservice.utils.Response;
import com.example.reportservice.utils.UnauthorizedException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import javax.security.auth.login.CredentialException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.reportservice.utils.ResponseStatusException;

@ControllerAdvice
public class GlobalException {

    private ResponseEntity<Response> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Response.error(status.value(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGlobalException(Exception ex, WebRequest request) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<Response> handleResourceNotFound(ChangeSetPersister.NotFoundException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(CredentialException.class)
    public ResponseEntity<Response> handleBadCredentials(CredentialException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.computeIfAbsent(fieldError.getField(), k -> new java.util.ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Response.error(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Response> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

}