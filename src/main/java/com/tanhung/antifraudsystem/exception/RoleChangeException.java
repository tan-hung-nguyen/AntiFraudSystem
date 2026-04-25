package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RoleChangeException extends AuthServiceException {
    public RoleChangeException(String message, HttpStatus status){
        super(message,status);
    }

}
