package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class IPAddressConflictException extends IPAddressException {
    public IPAddressConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
