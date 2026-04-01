package com.tanhung.antifraudsystem.exceptionHandler;

import com.tanhung.antifraudsystem.dto.response.ErrorResponse;
import com.tanhung.antifraudsystem.exception.InvalidAmountException;
import com.tanhung.antifraudsystem.exception.RegistrationException;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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


    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationConflictException(RegistrationException e){

        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(HttpStatus.CONFLICT.value());
        error.setError(HttpStatus.CONFLICT.getReasonPhrase());
        error.setDetails(e.getMessage());
        error.setTimestamp(Instant.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidValueFormatException(HttpMessageNotReadableException e){

        ErrorResponse error = new ErrorResponse();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setDetails("Invalid Format!");
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
}
