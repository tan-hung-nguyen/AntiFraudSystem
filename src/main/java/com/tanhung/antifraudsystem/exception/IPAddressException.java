package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class IPAddressException extends RuntimeException {
    private final HttpStatus status;
    public IPAddressException(String message, HttpStatus status) {
        this.status = status;
        super(message);
    }
}
