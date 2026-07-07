package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;
public class IPAddressException extends AntiFraudServiceException {
    protected IPAddressException(String message, HttpStatus status) {
        super(message, status);
    }
}
