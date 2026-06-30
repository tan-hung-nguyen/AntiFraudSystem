package com.tanhung.antifraudsystem.exception;

public class PhoneNumberConflictException extends RuntimeException {
  public PhoneNumberConflictException(String message) {
    super(message);
  }
}
