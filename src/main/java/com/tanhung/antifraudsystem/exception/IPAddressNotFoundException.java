package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class IPAddressNotFoundException extends IPAddressException {
    public IPAddressNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
