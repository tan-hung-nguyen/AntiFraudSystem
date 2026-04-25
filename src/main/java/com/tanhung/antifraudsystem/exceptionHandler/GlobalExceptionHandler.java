package com.tanhung.antifraudsystem.exceptionHandler;

import com.tanhung.antifraudsystem.dto.response.ErrorResponse;
import com.tanhung.antifraudsystem.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> handleJacksonException(Throwable cause){
        ErrorResponse error = new ErrorResponse();
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(Instant.now());
        if(cause instanceof UnrecognizedPropertyException e){
            error.setDetails("Unknown field: " + e.getPropertyName());
        } else if (cause instanceof InvalidFormatException e){
            error.setDetails(e.getPath().getFirst().getPropertyName() + ": Invalid format!");
        } else {
            error.setDetails(cause.getMessage());
        }

        return ResponseEntity.badRequest().body(error);
    }

    private ResponseEntity<ErrorResponse> getErrorResponse(RuntimeException e, HttpStatus status){
        ErrorResponse error = new ErrorResponse();
        error.setError(status.getReasonPhrase());
        error.setStatusCode(status.value());
        error.setTimestamp(Instant.now());
        error.setDetails(e.getMessage());

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("errors", errors);
        response.put("statusCode" , HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException e){
        return getErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNumberFormatException(NumberFormatException e){
        return getErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmountException(InvalidAmountException e) {
        return getErrorResponse(e, e.getStatus());
    }

    @ExceptionHandler({UserActiveStatusException.class, RegistrationException.class, RoleChangeException.class})
    public ResponseEntity<ErrorResponse> handleAuthServiceException(AuthServiceException e){
        return getErrorResponse(e, e.getStatus());
    }

    @ExceptionHandler({IPAddressException.class, StolenCardException.class})
    public ResponseEntity<ErrorResponse> handleAntiFraudServiceException(AntiFraudServiceException e){
        return getErrorResponse(e, e.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e){
        return getErrorResponse(e, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return handleJacksonException(e.getCause());
    }
}
