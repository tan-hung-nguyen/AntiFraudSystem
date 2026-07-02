package com.tanhung.antifraudsystem.record;

import com.tanhung.antifraudsystem.enums.TransactionResult;

public record RuleViolation(TransactionResult severity, String cause) {
}
