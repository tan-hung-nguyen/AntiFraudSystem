package com.tanhung.antifraudsystem.validators;

import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import com.tanhung.antifraudsystem.repo.SuspiciousIPRepo;
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
class SuspiciousIpValidatorTest {

    private static final String SUSPICIOUS_IP_ADDRESS = "192.168.1.100";
    private static final String CLEAN_IP_ADDRESS = "10.0.0.1";
    private static final String IP_VIOLATION_CAUSE = "ip";

    @Mock
    private SuspiciousIPRepo suspiciousIPRepo;

    @InjectMocks
    private SuspiciousIpValidator suspiciousIpValidator;

    @Test
    @DisplayName("Should return a PROHIBITED violation naming the ip when the transaction's ip address is suspicious")
    void shouldReturnProhibitedViolation_whenTransactionIpAddressIsSuspicious() {
        Transaction transaction = Transaction.builder()
                .ipAddress(SUSPICIOUS_IP_ADDRESS)
                .build();

        Mockito.when(suspiciousIPRepo.existsByIpAddress(SUSPICIOUS_IP_ADDRESS)).thenReturn(true);

        Optional<RuleViolation> actualViolation = suspiciousIpValidator.checkIpAddress(transaction);

        assertTrue(actualViolation.isPresent());
        assertEquals(TransactionResult.PROHIBITED, actualViolation.get().severity());
        assertEquals(IP_VIOLATION_CAUSE, actualViolation.get().cause());
    }

    @Test
    @DisplayName("Should return empty when the transaction's ip address is not suspicious")
    void shouldReturnEmpty_whenTransactionIpAddressIsNotSuspicious() {
        Transaction transaction = Transaction.builder()
                .ipAddress(CLEAN_IP_ADDRESS)
                .build();

        Mockito.when(suspiciousIPRepo.existsByIpAddress(CLEAN_IP_ADDRESS)).thenReturn(false);

        Optional<RuleViolation> actualViolation = suspiciousIpValidator.checkIpAddress(transaction);

        assertTrue(actualViolation.isEmpty());
    }
}