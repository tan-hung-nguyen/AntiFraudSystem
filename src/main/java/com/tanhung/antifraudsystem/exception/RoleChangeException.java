package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RoleChangeException extends AuthServiceException {
    public RoleChangeException(String message, HttpStatus status){
        super(message,status);
    }

}
