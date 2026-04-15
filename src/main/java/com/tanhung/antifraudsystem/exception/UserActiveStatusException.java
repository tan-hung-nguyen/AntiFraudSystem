package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class UserActiveStatusException extends AuthServiceException {
    public UserActiveStatusException(String message, HttpStatus status) {
        super(message,status);
    }
}
