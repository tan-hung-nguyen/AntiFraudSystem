package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class IPAddressNotFoundException extends IPAddressException {
    public IPAddressNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}
