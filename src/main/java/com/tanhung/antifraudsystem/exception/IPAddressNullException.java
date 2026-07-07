package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class IPAddressNullException extends IPAddressException {
    public IPAddressNullException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
