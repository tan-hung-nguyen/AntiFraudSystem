package com.tanhung.antifraudsystem.validators;

import com.tanhung.antifraudsystem.dto.response.ActionResponseDto;
import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionValidatorTest {

    private static final String CARD_NUMBER = "4000008449433403";
    private static final String IP_ADDRESS = "192.168.1.100";

    private static final BigDecimal AMOUNT_BELOW_ALLOWED_BOUNDARY = new BigDecimal("50");
    private static final BigDecimal AMOUNT_AT_ALLOWED_BOUNDARY = new BigDecimal("200");
    private static final BigDecimal AMOUNT_JUST_ABOVE_ALLOWED_BOUNDARY = new BigDecimal("200.01");
    private static final BigDecimal AMOUNT_AT_MANUAL_PROCESSING_BOUNDARY = new BigDecimal("1500");
    private static final BigDecimal AMOUNT_JUST_ABOVE_MANUAL_PROCESSING_BOUNDARY = new BigDecimal("1500.01");

    private static final String NONE_CAUSE = "none";
    private static final String AMOUNT_CAUSE = "amount";
    private static final String CARD_NUMBER_CAUSE = "card-number";
    private static final String IP_CAUSE = "ip";
    private static final String IP_CORRELATION_CAUSE = "ip-correlation";
    private static final String REGION_CORRELATION_CAUSE = "region-correlation";

    @Mock
    private CorrelationValidator correlationValidator;

    @Mock
    private StolenCardValidator stolenCardValidator;

    @Mock
    private SuspiciousIpValidator suspiciousIpValidator;

    @InjectMocks
    private TransactionValidator transactionValidator;

    private Transaction buildTransaction(BigDecimal amount) {
        return Transaction.builder()
                .amount(amount)
                .cardNumber(CARD_NUMBER)
                .ipAddress(IP_ADDRESS)
                .build();
    }

    private void stubValidators(Transaction transaction,
                                 Optional<RuleViolation> stolenCardViolation,
                                 Optional<RuleViolation> suspiciousIpViolation,
                                 Optional<RuleViolation> ipCorrelationViolation,
                                 Optional<RuleViolation> regionCorrelationViolation) {
        Mockito.when(stolenCardValidator.checkCardNumber(transaction)).thenReturn(stolenCardViolation);
        Mockito.when(suspiciousIpValidator.checkIpAddress(transaction)).thenReturn(suspiciousIpViolation);
        Mockito.when(correlationValidator.checkIpCorrelation(transaction)).thenReturn(ipCorrelationViolation);
        Mockito.when(correlationValidator.checkRegionCorrelation(transaction)).thenReturn(regionCorrelationViolation);
    }

    private void stubNoOtherViolations(Transaction transaction) {
        stubValidators(transaction, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Nested
    @DisplayName("validate() method - amount thresholds")
    class ValidateAmountThresholdTest {

        @Test
        @DisplayName("Should return ALLOWED with cause none when the amount is well below the allowed boundary")
        void shouldReturnAllowed_whenAmountIsWellBelowAllowedBoundary() {
            Transaction transaction = buildTransaction(AMOUNT_BELOW_ALLOWED_BOUNDARY);
            stubNoOtherViolations(transaction);

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.ALLOWED, actualResponse.getResult());
            assertEquals(NONE_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should return ALLOWED with cause none when the amount is exactly at the allowed boundary of 200")
        void shouldReturnAllowed_whenAmountIsAtAllowedBoundary() {
            Transaction transaction = buildTransaction(AMOUNT_AT_ALLOWED_BOUNDARY);
            stubNoOtherViolations(transaction);

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.ALLOWED, actualResponse.getResult());
            assertEquals(NONE_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should return MANUAL_PROCESSING with cause amount when the amount is just above the allowed boundary")
        void shouldReturnManualProcessing_whenAmountIsJustAboveAllowedBoundary() {
            Transaction transaction = buildTransaction(AMOUNT_JUST_ABOVE_ALLOWED_BOUNDARY);
            stubNoOtherViolations(transaction);

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.MANUAL_PROCESSING, actualResponse.getResult());
            assertEquals(AMOUNT_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should return MANUAL_PROCESSING with cause amount when the amount is exactly at the manual-processing boundary of 1500")
        void shouldReturnManualProcessing_whenAmountIsAtManualProcessingBoundary() {
            Transaction transaction = buildTransaction(AMOUNT_AT_MANUAL_PROCESSING_BOUNDARY);
            stubNoOtherViolations(transaction);

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.MANUAL_PROCESSING, actualResponse.getResult());
            assertEquals(AMOUNT_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should return PROHIBITED with cause amount when the amount is just above the manual-processing boundary")
        void shouldReturnProhibited_whenAmountIsJustAboveManualProcessingBoundary() {
            Transaction transaction = buildTransaction(AMOUNT_JUST_ABOVE_MANUAL_PROCESSING_BOUNDARY);
            stubNoOtherViolations(transaction);

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.PROHIBITED, actualResponse.getResult());
            assertEquals(AMOUNT_CAUSE, actualResponse.getInfo());
        }
    }

    @Nested
    @DisplayName("validate() method - rule violation combination")
    class ValidateRuleViolationCombinationTest {

        @Test
        @DisplayName("Should return PROHIBITED with cause card-number when only the card number is stolen")
        void shouldReturnProhibited_whenOnlyCardNumberIsStolen() {
            Transaction transaction = buildTransaction(AMOUNT_BELOW_ALLOWED_BOUNDARY);
            RuleViolation stolenCardViolation = new RuleViolation(TransactionResult.PROHIBITED, CARD_NUMBER_CAUSE);
            stubValidators(transaction, Optional.of(stolenCardViolation),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty());

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.PROHIBITED, actualResponse.getResult());
            assertEquals(CARD_NUMBER_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should return PROHIBITED with cause ip when only the ip address is suspicious")
        void shouldReturnProhibited_whenOnlyIpAddressIsSuspicious() {
            Transaction transaction = buildTransaction(AMOUNT_BELOW_ALLOWED_BOUNDARY);
            RuleViolation suspiciousIpViolation = new RuleViolation(TransactionResult.PROHIBITED, IP_CAUSE);
            stubValidators(transaction, Optional.empty(), Optional.of(suspiciousIpViolation), Optional.empty(), Optional.empty());

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.PROHIBITED, actualResponse.getResult());
            assertEquals(IP_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should return MANUAL_PROCESSING with cause ip-correlation when only the ip correlation check reports manual processing")
        void shouldReturnManualProcessing_whenOnlyIpCorrelationReportsManualProcessing() {
            Transaction transaction = buildTransaction(AMOUNT_BELOW_ALLOWED_BOUNDARY);
            RuleViolation ipCorrelationViolation = new RuleViolation(TransactionResult.MANUAL_PROCESSING, IP_CORRELATION_CAUSE);
            stubValidators(transaction, Optional.empty(), Optional.empty(), Optional.of(ipCorrelationViolation), Optional.empty());

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.MANUAL_PROCESSING, actualResponse.getResult());
            assertEquals(IP_CORRELATION_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should return PROHIBITED with cause region-correlation when only the region correlation check reports prohibited")
        void shouldReturnProhibited_whenOnlyRegionCorrelationReportsProhibited() {
            Transaction transaction = buildTransaction(AMOUNT_BELOW_ALLOWED_BOUNDARY);
            RuleViolation regionCorrelationViolation = new RuleViolation(TransactionResult.PROHIBITED, REGION_CORRELATION_CAUSE);
            stubValidators(transaction, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(regionCorrelationViolation));

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.PROHIBITED, actualResponse.getResult());
            assertEquals(REGION_CORRELATION_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should combine causes of every violation that matches the highest severity when multiple checks report the same top severity")
        void shouldCombineCauses_whenMultipleChecksReportSameTopSeverity() {
            Transaction transaction = buildTransaction(AMOUNT_BELOW_ALLOWED_BOUNDARY);
            RuleViolation stolenCardViolation = new RuleViolation(TransactionResult.PROHIBITED, CARD_NUMBER_CAUSE);
            RuleViolation suspiciousIpViolation = new RuleViolation(TransactionResult.PROHIBITED, IP_CAUSE);
            stubValidators(transaction,
                    Optional.of(stolenCardViolation),
                    Optional.of(suspiciousIpViolation),
                    Optional.empty(),
                    Optional.empty());

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.PROHIBITED, actualResponse.getResult());
            assertEquals(CARD_NUMBER_CAUSE + ", " + IP_CAUSE, actualResponse.getInfo());
        }

        @Test
        @DisplayName("Should exclude causes of lower-severity violations from the info string when a higher-severity violation exists")
        void shouldExcludeLowerSeverityCauses_whenHigherSeverityViolationExists() {
            Transaction transaction = buildTransaction(AMOUNT_BELOW_ALLOWED_BOUNDARY);
            RuleViolation stolenCardViolation = new RuleViolation(TransactionResult.PROHIBITED, CARD_NUMBER_CAUSE);
            RuleViolation ipCorrelationViolation = new RuleViolation(TransactionResult.MANUAL_PROCESSING, IP_CORRELATION_CAUSE);
            stubValidators(transaction, Optional.of(stolenCardViolation), Optional.empty(), Optional.of(ipCorrelationViolation), Optional.empty());

            ActionResponseDto actualResponse = transactionValidator.validate(transaction);

            assertEquals(TransactionResult.PROHIBITED, actualResponse.getResult());
            assertEquals(CARD_NUMBER_CAUSE, actualResponse.getInfo());
        }
    }
}