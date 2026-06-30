package com.tanhung.antifraudsystem.exception;

public class EmailConflictException extends RuntimeException {
  public EmailConflictException(String message) {
    super(message);
  }
}
