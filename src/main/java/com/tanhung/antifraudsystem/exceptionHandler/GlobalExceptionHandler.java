package com.tanhung.antifraudsystem.exceptionHandler;

import com.tanhung.antifraudsystem.dto.response.ErrorResponse;
import com.tanhung.antifraudsystem.exception.*;
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

        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setDetails(e.getMessage());
        error.setTimestamp(Instant.now());

        return ResponseEntity.badRequest().body(error);
    }


    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException e){

        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setDetails(e.getPath().getFirst().getPropertyName() +  ": Invalid Format!");
        error.setTimestamp(Instant.now());

        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(NumberFormatException e){

        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setDetails(e.getMessage());
        error.setTimestamp(Instant.now());

        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmountException(InvalidAmountException e) {
        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setTimestamp(Instant.now());
        error.setDetails(e.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<ErrorResponse> handleUnrecognizedPropertyException(UnrecognizedPropertyException e) {

        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(Instant.now());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setDetails("Unknown field: " + e.getPropertyName());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({UserActiveStatusException.class, RegistrationException.class, RoleChangeException.class})
    public ResponseEntity<ErrorResponse> handleAuthServiceException(AuthServiceException e){

        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(e.getStatus().value());
        error.setError(e.getStatus().getReasonPhrase());
        error.setDetails(e.getMessage());
        error.setTimestamp(Instant.now());

        return ResponseEntity.status(e.getStatus()).body(error);
    }


    @ExceptionHandler({IPAddressException.class, StolenCardException.class})
    public ResponseEntity<ErrorResponse> handleAntiFraudServiceException(AntiFraudServiceException e){
        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(e.getStatus().value());
        error.setError(e.getStatus().getReasonPhrase());
        error.setDetails(e.getMessage());
        error.setTimestamp(Instant.now());

        return ResponseEntity.status(e.getStatus()).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setDetails(e.getMessage());
        error.setTimestamp(Instant.now());

        return ResponseEntity.badRequest().body(error);
    }
}
