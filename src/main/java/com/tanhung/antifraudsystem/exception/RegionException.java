package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RegionException extends AntiFraudServiceException {
    public RegionException(String message, HttpStatus status) {
        super(message, status);
    }
}
