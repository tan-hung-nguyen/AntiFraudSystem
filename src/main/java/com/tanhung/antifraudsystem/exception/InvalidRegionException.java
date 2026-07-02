package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidRegionException extends RegionException {
    public InvalidRegionException(String message, HttpStatus status) {
        super(message, status);
    }
}
