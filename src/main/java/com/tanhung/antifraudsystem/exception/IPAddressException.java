package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;
public class IPAddressException extends AntiFraudServiceException {
    public IPAddressException(String message, HttpStatus status) {
        super(message, status);
    }
}
