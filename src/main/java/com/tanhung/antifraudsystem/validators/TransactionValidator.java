package com.tanhung.antifraudsystem.validator;

import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import com.tanhung.antifraudsystem.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class TransactionValidator {
    private final CorrelationValidator correlationValidator;
    private final StolenCardValidator stolenCardValidator;
    private final SuspiciousIpValidator suspiciousIpValidator;


}
