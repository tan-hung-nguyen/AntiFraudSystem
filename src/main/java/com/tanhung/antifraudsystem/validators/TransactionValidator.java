package com.tanhung.antifraudsystem.validators;

import com.tanhung.antifraudsystem.dto.response.ActionResponseDto;
import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class TransactionValidator {
    private final CorrelationValidator correlationValidator;
    private final StolenCardValidator stolenCardValidator;
    private final SuspiciousIpValidator suspiciousIpValidator;

    public ActionResponseDto validate(Transaction transaction){
        List<RuleViolation> violations = Stream.of(
                checkAmount(transaction.getAmount()),
                stolenCardValidator.checkCardNumber(transaction),
                suspiciousIpValidator.checkIpAddress(transaction),
                correlationValidator.checkIpCorrelation(transaction),
                correlationValidator.checkRegionCorrelation(transaction))
                .flatMap(Optional::stream)
                .toList();
        if(violations.isEmpty()){
            return new ActionResponseDto(TransactionResult.ALLOWED, "none");
        }
        TransactionResult finalResult = violations.stream()
                .map(RuleViolation::severity)
                .max(Comparator.comparingInt(TransactionResult::ordinal))
                .orElse(TransactionResult.ALLOWED);
        String info = violations.stream()
                .map((violation) ->getCause(violation,finalResult))
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

        return new ActionResponseDto(finalResult, info);
    }

    private Optional<RuleViolation> checkAmount(BigDecimal amount){
        if(amount.compareTo(new BigDecimal(200)) <= 0){
            return Optional.of(new RuleViolation(TransactionResult.ALLOWED, "none"));
        } else if(amount.compareTo(new BigDecimal(1500)) <= 0) {
            return Optional.of(new RuleViolation(TransactionResult.MANUAL_PROCESSING, "amount"));
        } else {
            return Optional.of(new RuleViolation(TransactionResult.PROHIBITED, "amount"));
        }
    }

    private String getCause(RuleViolation ruleViolation, TransactionResult result){
        return ruleViolation.severity().equals(result) ? ruleViolation.cause() : "";
    }


}
