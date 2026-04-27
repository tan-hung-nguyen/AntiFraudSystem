package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class CardNumberInvalidException extends StolenCardException {
    public CardNumberInvalidException(String message, HttpStatus status) {
        super(message, status);
    }
}
