package com.tanhung.antifraudsystem.validator;

import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import com.tanhung.antifraudsystem.repo.StolenCardRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StolenCardValidator {
    private final StolenCardRepo stolenCardRepo;

    public Optional<RuleViolation> checkCardNumber(Transaction transaction){
        return toViolation(transaction.getCardNumber());
    }

    private Optional<RuleViolation> toViolation(String cardNumber){
        if(isStolenCard(cardNumber)){
            return Optional.of(new RuleViolation(TransactionResult.PROHIBITED, "card-number"));
        }
        return Optional.empty();
    }

    public boolean isStolenCard(String cardNumber){
        return stolenCardRepo.existsStolenCardByCardNumber(cardNumber);
    }
}
