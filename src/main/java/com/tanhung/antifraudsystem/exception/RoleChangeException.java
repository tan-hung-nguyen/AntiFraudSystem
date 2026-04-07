package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RoleChangeException extends RuntimeException {
    private final HttpStatus status;

    public RoleChangeException(String message, HttpStatus status){
        super(message);
        this.status = status;
    }

}
