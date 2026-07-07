package com.tanhung.antifraudsystem.validators;

import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.model.Region;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.record.RuleViolation;
import com.tanhung.antifraudsystem.repo.TransactionRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CorrelationValidatorTest {

    private static final String CARD_NUMBER = "4000008449433403";
    private static final String IP_ADDRESS = "192.168.1.100";

    private static final String TRANSACTION_REGION_CODE = "US";
    private static final String OTHER_REGION_CODE_1 = "CA";
    private static final String OTHER_REGION_CODE_2 = "MX";
    private static final String OTHER_REGION_CODE_3 = "FR";

    private static final String OTHER_IP_ADDRESS_1 = "10.0.0.1";
    private static final String OTHER_IP_ADDRESS_2 = "10.0.0.2";
    private static final String OTHER_IP_ADDRESS_3 = "10.0.0.3";

    private static final LocalDateTime TRANSACTION_DATE = LocalDateTime.of(2026, 7, 7, 12, 0, 0);
    private static final int CORRELATION_WINDOW_HOURS = 1;

    private static final String REGION_CORRELATION_REASON = "region-correlation";
    private static final String IP_CORRELATION_REASON = "ip-correlation";

    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private CorrelationValidator correlationValidator;

    private Transaction buildTransaction(String cardNumber, String ipAddress, String regionCode, LocalDateTime date) {
        Region region = Region.builder().code(regionCode).build();
        return Transaction.builder()
                .cardNumber(cardNumber)
                .ipAddress(ipAddress)
                .region(region)
                .date(date)
                .build();
    }

    @Nested
    @DisplayName("checkRegionCorrelation() method")
    class CheckRegionCorrelationMethodTest {

        @Test
        @DisplayName("Should return empty when there are no other distinct regions for the card in the past hour")
        void shouldReturnEmpty_whenThereAreNoOtherDistinctRegions() {
            Transaction transaction = buildTransaction(CARD_NUMBER, IP_ADDRESS, TRANSACTION_REGION_CODE, TRANSACTION_DATE);
            Mockito.when(transactionRepo.findDistinctRegionExcluding(
                    CARD_NUMBER, TRANSACTION_DATE.minusHours(CORRELATION_WINDOW_HOURS), TRANSACTION_DATE, TRANSACTION_REGION_CODE))
                    .thenReturn(Collections.emptyList());

            Optional<RuleViolation> actualViolation = correlationValidator.checkRegionCorrelation(transaction);

            assertTrue(actualViolation.isEmpty());
        }

        @Test
        @DisplayName("Should return empty when there is only one other distinct region")
        void shouldReturnEmpty_whenThereIsOnlyOneOtherDistinctRegion() {
            Transaction transaction = buildTransaction(CARD_NUMBER, IP_ADDRESS, TRANSACTION_REGION_CODE, TRANSACTION_DATE);
            Mockito.when(transactionRepo.findDistinctRegionExcluding(
                    CARD_NUMBER, TRANSACTION_DATE.minusHours(CORRELATION_WINDOW_HOURS), TRANSACTION_DATE, TRANSACTION_REGION_CODE))
                    .thenReturn(List.of(OTHER_REGION_CODE_1));

            Optional<RuleViolation> actualViolation = correlationValidator.checkRegionCorrelation(transaction);

            assertTrue(actualViolation.isEmpty());
        }

        @Test
        @DisplayName("Should return a MANUAL_PROCESSING violation when there are exactly two other distinct regions")
        void shouldReturnManualProcessingViolation_whenThereAreExactlyTwoOtherDistinctRegions() {
            Transaction transaction = buildTransaction(CARD_NUMBER, IP_ADDRESS, TRANSACTION_REGION_CODE, TRANSACTION_DATE);
            Mockito.when(transactionRepo.findDistinctRegionExcluding(
                    CARD_NUMBER, TRANSACTION_DATE.minusHours(CORRELATION_WINDOW_HOURS), TRANSACTION_DATE, TRANSACTION_REGION_CODE))
                    .thenReturn(List.of(OTHER_REGION_CODE_1, OTHER_REGION_CODE_2));

            Optional<RuleViolation> actualViolation = correlationValidator.checkRegionCorrelation(transaction);

            assertTrue(actualViolation.isPresent());
            assertEquals(TransactionResult.MANUAL_PROCESSING, actualViolation.get().severity());
            assertEquals(REGION_CORRELATION_REASON, actualViolation.get().cause());
        }

        @Test
        @DisplayName("Should return a PROHIBITED violation when there are more than two other distinct regions")
        void shouldReturnProhibitedViolation_whenThereAreMoreThanTwoOtherDistinctRegions() {
            Transaction transaction = buildTransaction(CARD_NUMBER, IP_ADDRESS, TRANSACTION_REGION_CODE, TRANSACTION_DATE);
            Mockito.when(transactionRepo.findDistinctRegionExcluding(
                    CARD_NUMBER, TRANSACTION_DATE.minusHours(CORRELATION_WINDOW_HOURS), TRANSACTION_DATE, TRANSACTION_REGION_CODE))
                    .thenReturn(List.of(OTHER_REGION_CODE_1, OTHER_REGION_CODE_2, OTHER_REGION_CODE_3));

            Optional<RuleViolation> actualViolation = correlationValidator.checkRegionCorrelation(transaction);

            assertTrue(actualViolation.isPresent());
            assertEquals(TransactionResult.PROHIBITED, actualViolation.get().severity());
            assertEquals(REGION_CORRELATION_REASON, actualViolation.get().cause());
        }
    }

    @Nested
    @DisplayName("checkIpCorrelation() method")
    class CheckIpCorrelationMethodTest {

        @Test
        @DisplayName("Should return empty when there are no other distinct ip addresses for the card in the past hour")
        void shouldReturnEmpty_whenThereAreNoOtherDistinctIpAddresses() {
            Transaction transaction = buildTransaction(CARD_NUMBER, IP_ADDRESS, TRANSACTION_REGION_CODE, TRANSACTION_DATE);
            Mockito.when(transactionRepo.findDistinctIpsExcluding(
                    CARD_NUMBER, TRANSACTION_DATE.minusHours(CORRELATION_WINDOW_HOURS), TRANSACTION_DATE, IP_ADDRESS))
                    .thenReturn(Collections.emptyList());

            Optional<RuleViolation> actualViolation = correlationValidator.checkIpCorrelation(transaction);

            assertTrue(actualViolation.isEmpty());
        }

        @Test
        @DisplayName("Should return empty when there is only one other distinct ip address")
        void shouldReturnEmpty_whenThereIsOnlyOneOtherDistinctIpAddress() {
            Transaction transaction = buildTransaction(CARD_NUMBER, IP_ADDRESS, TRANSACTION_REGION_CODE, TRANSACTION_DATE);
            Mockito.when(transactionRepo.findDistinctIpsExcluding(
                    CARD_NUMBER, TRANSACTION_DATE.minusHours(CORRELATION_WINDOW_HOURS), TRANSACTION_DATE, IP_ADDRESS))
                    .thenReturn(List.of(OTHER_IP_ADDRESS_1));

            Optional<RuleViolation> actualViolation = correlationValidator.checkIpCorrelation(transaction);

            assertTrue(actualViolation.isEmpty());
        }

        @Test
        @DisplayName("Should return a MANUAL_PROCESSING violation when there are exactly two other distinct ip addresses")
        void shouldReturnManualProcessingViolation_whenThereAreExactlyTwoOtherDistinctIpAddresses() {
            Transaction transaction = buildTransaction(CARD_NUMBER, IP_ADDRESS, TRANSACTION_REGION_CODE, TRANSACTION_DATE);
            Mockito.when(transactionRepo.findDistinctIpsExcluding(
                    CARD_NUMBER, TRANSACTION_DATE.minusHours(CORRELATION_WINDOW_HOURS), TRANSACTION_DATE, IP_ADDRESS))
                    .thenReturn(List.of(OTHER_IP_ADDRESS_1, OTHER_IP_ADDRESS_2));

            Optional<RuleViolation> actualViolation = correlationValidator.checkIpCorrelation(transaction);

            assertTrue(actualViolation.isPresent());
            assertEquals(TransactionResult.MANUAL_PROCESSING, actualViolation.get().severity());
            assertEquals(IP_CORRELATION_REASON, actualViolation.get().cause());
        }

        @Test
        @DisplayName("Should return a PROHIBITED violation when there are more than two other distinct ip addresses")
        void shouldReturnProhibitedViolation_whenThereAreMoreThanTwoOtherDistinctIpAddresses() {
            Transaction transaction = buildTransaction(CARD_NUMBER, IP_ADDRESS, TRANSACTION_REGION_CODE, TRANSACTION_DATE);
            Mockito.when(transactionRepo.findDistinctIpsExcluding(
                    CARD_NUMBER, TRANSACTION_DATE.minusHours(CORRELATION_WINDOW_HOURS), TRANSACTION_DATE, IP_ADDRESS))
                    .thenReturn(List.of(OTHER_IP_ADDRESS_1, OTHER_IP_ADDRESS_2, OTHER_IP_ADDRESS_3));

            Optional<RuleViolation> actualViolation = correlationValidator.checkIpCorrelation(transaction);

            assertTrue(actualViolation.isPresent());
            assertEquals(TransactionResult.PROHIBITED, actualViolation.get().severity());
            assertEquals(IP_CORRELATION_REASON, actualViolation.get().cause());
        }
    }
}
