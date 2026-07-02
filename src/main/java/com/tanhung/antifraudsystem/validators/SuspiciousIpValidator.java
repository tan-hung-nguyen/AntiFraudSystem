package com.tanhung.antifraudsystem.validators;

import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import com.tanhung.antifraudsystem.repo.SuspiciousIPRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SuspiciousIpValidator {
    private final SuspiciousIPRepo suspiciousIPRepo;

    public Optional<RuleViolation> checkIpAddress(Transaction transaction){
        return toViolation(transaction.getIpAddress());
    }

    private Optional<RuleViolation> toViolation(String ip){
        if(isSuspiciousIp(ip)){
            return Optional.of(new RuleViolation(TransactionResult.PROHIBITED, "ip"));
        }
        return Optional.empty();
    }

    public boolean isSuspiciousIp(String ip){
        return suspiciousIPRepo.existsByIpAddress(ip);
    }
}
