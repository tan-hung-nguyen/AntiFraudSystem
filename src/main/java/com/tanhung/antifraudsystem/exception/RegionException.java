package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RegionException extends AntiFraudServiceException {
    public RegionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
