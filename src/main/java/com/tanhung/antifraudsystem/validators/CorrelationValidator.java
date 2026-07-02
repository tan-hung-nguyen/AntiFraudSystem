package com.tanhung.antifraudsystem.validators;

import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import com.tanhung.antifraudsystem.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CorrelationValidator {
    private final TransactionRepo transactionRepo;

    public Optional<RuleViolation> checkRegionCorrelation(Transaction transaction){
        List<String> otherRegions = transactionRepo.findDistinctRegionExcluding(
                transaction.getCardNumber(),
                transaction.getDate().minusHours(1),
                transaction.getDate(),
                transaction.getRegion().getCode()
        );

        return toViolation(otherRegions.size(), "region-correlation");
    }

    public Optional<RuleViolation> checkIpCorrelation(Transaction transaction){
        List<String> otherIps = transactionRepo.findDistinctIpsExcluding(
                transaction.getCardNumber(),
                transaction.getDate().minusHours(1),
                transaction.getDate(),
                transaction.getIpAddress()
        );

        return toViolation(otherIps.size(), "ip-correlation");
    }

    private Optional<RuleViolation> toViolation(int distinctCount, String reasonCode){
        if (distinctCount > 2) {
            return Optional.of(new RuleViolation(TransactionResult.PROHIBITED, reasonCode));
        }
        if (distinctCount == 2) {
            return Optional.of(new RuleViolation(TransactionResult.MANUAL_PROCESSING, reasonCode));
        }
        return Optional.empty();
    }
}
