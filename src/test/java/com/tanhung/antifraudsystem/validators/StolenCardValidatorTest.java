package com.tanhung.antifraudsystem.validators;

import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import com.tanhung.antifraudsystem.repo.StolenCardRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StolenCardValidatorTest {

    private static final String STOLEN_CARD_NUMBER = "4000008449433403";
    private static final String CLEAN_CARD_NUMBER = "5100008449433412";
    private static final String CARD_NUMBER_VIOLATION_CAUSE = "card-number";

    @Mock
    private StolenCardRepo stolenCardRepo;

    @InjectMocks
    private StolenCardValidator stolenCardValidator;

    @Test
    @DisplayName("Should return a PROHIBITED violation naming the card number when the transaction's card number is stolen")
    void shouldReturnProhibitedViolation_whenTransactionCardNumberIsStolen() {
        Transaction transaction = Transaction.builder()
                .cardNumber(STOLEN_CARD_NUMBER)
                .build();

        Mockito.when(stolenCardRepo.existsStolenCardByCardNumber(STOLEN_CARD_NUMBER)).thenReturn(true);

        Optional<RuleViolation> actualViolation = stolenCardValidator.checkCardNumber(transaction);

        assertTrue(actualViolation.isPresent());
        assertEquals(TransactionResult.PROHIBITED, actualViolation.get().severity());
        assertEquals(CARD_NUMBER_VIOLATION_CAUSE, actualViolation.get().cause());
    }

    @Test
    @DisplayName("Should return empty when the transaction's card number is not stolen")
    void shouldReturnEmpty_whenTransactionCardNumberIsNotStolen() {
        Transaction transaction = Transaction.builder()
                .cardNumber(CLEAN_CARD_NUMBER)
                .build();

        Mockito.when(stolenCardRepo.existsStolenCardByCardNumber(CLEAN_CARD_NUMBER)).thenReturn(false);

        Optional<RuleViolation> actualViolation = stolenCardValidator.checkCardNumber(transaction);

        assertTrue(actualViolation.isEmpty());
    }
}