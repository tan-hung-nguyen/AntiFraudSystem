package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.TransactionRequestDto;
import com.tanhung.antifraudsystem.dto.response.ActionResponseDto;
import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.exception.CardNumberNullException;
import com.tanhung.antifraudsystem.exception.InvalidCardNumberException;
import com.tanhung.antifraudsystem.exception.InvalidRegionException;
import com.tanhung.antifraudsystem.model.Region;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.repo.RegionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionReviewServiceTest {

    // Digits before the check digit sum to 57 under CardNumberValidator's algorithm,
    // so any card number sharing this prefix is valid only when its last digit divides 57 evenly.
    private static final String CARD_NUMBER_WITH_VALID_CHECK_DIGIT = "4000008449433403";
    private static final String CARD_NUMBER_WITH_INVALID_CHECK_DIGIT = "4000008449433404";
    private static final String EMPTY_CARD_NUMBER = "";
    private static final String NULL_CARD_NUMBER = null;

    private static final String EXISTING_REGION_CODE = "EAP";
    private static final String NON_EXISTENT_REGION_CODE = "XX";

    private static final Integer REGION_ID = 1;
    private static final String REGION_DESCRIPTION = "East Asia and Pacific";

    private static final BigDecimal TRANSACTION_AMOUNT = new BigDecimal("150.00");
    private static final String TRANSACTION_IP_ADDRESS = "192.168.1.1";
    private static final String TRANSACTION_DATE = "2022-01-22T16:04:00";

    private static final String ALLOWED_INFO = "none";

    @Mock
    private RegionRepo regionRepo;
    @Mock
    private TransactionManagerService transactionManager;
    @InjectMocks
    private TransactionReviewService transactionReviewService;

    private TransactionRequestDto validRequest;
    private Region existingRegion;
    private Transaction mappedTransaction;

    @BeforeEach
    void setUp() {
        validRequest = TransactionRequestDto.builder()
                .amount(TRANSACTION_AMOUNT)
                .ipAddress(TRANSACTION_IP_ADDRESS)
                .cardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT)
                .region(EXISTING_REGION_CODE)
                .date(TRANSACTION_DATE)
                .build();

        existingRegion = Region.builder()
                .id(REGION_ID)
                .code(EXISTING_REGION_CODE)
                .description(REGION_DESCRIPTION)
                .build();

        mappedTransaction = Transaction.builder()
                .amount(TRANSACTION_AMOUNT)
                .ipAddress(TRANSACTION_IP_ADDRESS)
                .cardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT)
                .build();
    }

    @Nested
    @DisplayName("reviewRequest method")
    class ReviewRequestTest {

        @Test
        @DisplayName("Should assign the resolved region, persist the transaction and " +
                "return the action response when the card number and region are valid")
        void shouldReturnActionResponseAndSaveTransaction_whenCardNumberAndRegionAreValid() {
            ActionResponseDto expectedResponse = ActionResponseDto.builder()
                    .result(TransactionResult.ALLOWED)
                    .info(ALLOWED_INFO)
                    .build();

            Mockito.when(transactionManager.convertToEntity(validRequest)).thenReturn(mappedTransaction);
            Mockito.when(regionRepo.findByCode(EXISTING_REGION_CODE)).thenReturn(existingRegion);
            Mockito.when(transactionManager.proceedTransaction(mappedTransaction)).thenReturn(expectedResponse);

            ActionResponseDto actualResponse = transactionReviewService.reviewRequest(validRequest);

            assertSame(expectedResponse, actualResponse);
            assertSame(existingRegion, mappedTransaction.getRegion());

            ArgumentCaptor<Transaction> savedTransactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            Mockito.verify(transactionManager).saveTransaction(savedTransactionCaptor.capture());
            assertSame(existingRegion, savedTransactionCaptor.getValue().getRegion());

            InOrderVerifier.verifyProceedsBeforeSaving(transactionManager, mappedTransaction);
        }

        @Test
        @DisplayName("Should throw InvalidCardNumberException without touching the region " +
                "or transaction manager when the card number fails the check-digit validation")
        void shouldThrowInvalidCardNumberException_whenCardNumberFailsCheckDigitValidation() {
            validRequest.setCardNumber(CARD_NUMBER_WITH_INVALID_CHECK_DIGIT);

            InvalidCardNumberException exception = assertThrows(InvalidCardNumberException.class,
                    () -> transactionReviewService.reviewRequest(validRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            Mockito.verifyNoInteractions(regionRepo, transactionManager);
        }

        @Test
        @DisplayName("Should throw InvalidRegionException naming the unresolved region code " +
                "when no region matches the request, without proceeding or saving the transaction")
        void shouldThrowInvalidRegionException_whenRegionCodeDoesNotExist() {
            validRequest.setRegion(NON_EXISTENT_REGION_CODE);

            Mockito.when(transactionManager.convertToEntity(validRequest)).thenReturn(mappedTransaction);
            Mockito.when(regionRepo.findByCode(NON_EXISTENT_REGION_CODE)).thenReturn(null);

            InvalidRegionException exception = assertThrows(InvalidRegionException.class,
                    () -> transactionReviewService.reviewRequest(validRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertTrue(exception.getMessage().contains(NON_EXISTENT_REGION_CODE));
            Mockito.verify(transactionManager, Mockito.never()).proceedTransaction(Mockito.any());
            Mockito.verify(transactionManager, Mockito.never()).saveTransaction(Mockito.any());
        }

        @Test
        @DisplayName("Should propagate InvalidCardNumberException without touching the region or " +
                "transaction manager when the card number is null")
        void shouldPropagateInvalidCardNumberException_whenCardNumberIsNull() {
            validRequest.setCardNumber(NULL_CARD_NUMBER);

            assertThrows(InvalidCardNumberException.class,
                    () -> transactionReviewService.reviewRequest(validRequest));

            Mockito.verifyNoInteractions(regionRepo, transactionManager);
        }

        @Test
        @DisplayName("Should propagate InvalidCardNumberException without touching the " +
                "region or transaction manager when the card number is empty")
        void shouldPropagateInvalidCardNumberException_whenCardNumberIsEmpty() {
            validRequest.setCardNumber(EMPTY_CARD_NUMBER);

            assertThrows(InvalidCardNumberException.class,
                    () -> transactionReviewService.reviewRequest(validRequest));

            Mockito.verifyNoInteractions(regionRepo, transactionManager);
        }
    }

    private static final class InOrderVerifier {
        static void verifyProceedsBeforeSaving(TransactionManagerService transactionManager, Transaction transaction) {
            org.mockito.InOrder inOrder = Mockito.inOrder(transactionManager);
            inOrder.verify(transactionManager).proceedTransaction(transaction);
            inOrder.verify(transactionManager).saveTransaction(transaction);
        }
    }
}
