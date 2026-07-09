package com.tanhung.antifraudsystem.exceptionHandler;

import com.tanhung.antifraudsystem.dto.response.ErrorResponseDto;
import com.tanhung.antifraudsystem.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponseDto> handleJacksonException(Throwable cause){
        ErrorResponseDto error = ErrorResponseDto
                                .builder()
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .timestamp(Instant.now())
                                .build();
        if(cause == null || cause.getMessage() == null){
            error.setDetails("Required request body is missing!");
        } else if(cause instanceof UnrecognizedPropertyException e){
            error.setDetails("Unknown field: " + e.getPropertyName());
        } else if (cause instanceof InvalidFormatException e){
            error.setDetails(e.getPath().getFirst().getPropertyName() + ": Invalid format!");
        } else {
            error.setDetails(cause.getMessage());
        }

        return ResponseEntity.badRequest().body(error);
    }

    private ResponseEntity<ErrorResponseDto> getErrorResponse(RuntimeException e, HttpStatus status){
        ErrorResponseDto error = ErrorResponseDto
                                .builder()
                                .statusCode(status.value())
                                .error(status.getReasonPhrase())
                                .details(e.getMessage())
                                .timestamp(Instant.now())
                                .build();
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
    public ResponseEntity<ErrorResponseDto> handleUsernameNotFoundException(UsernameNotFoundException e){
        return getErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidNumberFormatException(NumberFormatException e){
        return getErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthServiceException(AuthServiceException e){
        return getErrorResponse(e, e.getStatus());
    }

    @ExceptionHandler(AntiFraudServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleAntiFraudServiceException(AntiFraudServiceException e){
        return getErrorResponse(e, e.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException e){
        return getErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return handleJacksonException(e.getCause());
    }
}
