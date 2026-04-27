package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class IPAddressNullException extends IPAddressException {
    public IPAddressNullException(String message, HttpStatus status) {
        super(message, status);
    }
}
