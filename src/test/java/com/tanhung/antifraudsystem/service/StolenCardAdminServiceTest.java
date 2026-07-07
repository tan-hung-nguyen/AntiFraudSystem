package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.StolenCardNumberRequestDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponseDto;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.model.StolenCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StolenCardAdminServiceTest {

    // Digits before the check digit sum such that '3' is the only check digit satisfying
    // CardNumberValidator's Luhn-style formula; '4' therefore fails the check-digit validation.
    private static final String CARD_NUMBER_WITH_VALID_CHECK_DIGIT = "4000008449433403";
    private static final String CARD_NUMBER_WITH_INVALID_CHECK_DIGIT = "4000008449433404";
    private static final String NULL_CARD_NUMBER = null;
    private static final String EMPTY_CARD_NUMBER = "";

    private static final Long STOLEN_CARD_ID = 1L;

    @Mock
    private StolenCardService stolenCardService;

    @InjectMocks
    private StolenCardAdminService stolenCardAdminService;

    @Nested
    @DisplayName("addStolenCardNumber method")
    class AddStolenCardNumberTest {

        @Test
        @DisplayName("Should save the card and return its response dto when the card number passes check-digit validation")
        void shouldSaveCardAndReturnResponseDto_whenCardNumberIsValid() {
            StolenCardNumberRequestDto requestWithValidCardNumber = StolenCardNumberRequestDto.builder()
                    .cardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT)
                    .build();
            StolenCard savedCardEntity = StolenCard.builder()
                    .id(STOLEN_CARD_ID)
                    .cardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT)
                    .build();
            StolenCardResponseDto expectedResponse = StolenCardResponseDto.builder()
                    .id(STOLEN_CARD_ID)
                    .cardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT)
                    .build();

            Mockito.when(stolenCardService.addCard(requestWithValidCardNumber)).thenReturn(savedCardEntity);
            Mockito.when(stolenCardService.convertToDto(savedCardEntity)).thenReturn(expectedResponse);

            StolenCardResponseDto actualResponse = stolenCardAdminService.addStolenCardNumber(requestWithValidCardNumber);

            assertSame(expectedResponse, actualResponse);
            Mockito.verify(stolenCardService).addCard(requestWithValidCardNumber);
            Mockito.verify(stolenCardService).convertToDto(savedCardEntity);
        }

        @Test
        @DisplayName("Should throw StolenCardNullException without touching the service when the request is null")
        void shouldThrowStolenCardNullException_whenRequestIsNull() {
            StolenCardNullException exception = assertThrows(StolenCardNullException.class,
                    () -> stolenCardAdminService.addStolenCardNumber(null));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            Mockito.verifyNoInteractions(stolenCardService);
        }

        @Test
        @DisplayName("Should throw InvalidCardNumberException naming the card number without saving " +
                "when the card number fails check-digit validation")
        void shouldThrowInvalidCardNumberException_whenCardNumberFailsCheckDigitValidation() {
            StolenCardNumberRequestDto requestWithInvalidCardNumber = StolenCardNumberRequestDto.builder()
                    .cardNumber(CARD_NUMBER_WITH_INVALID_CHECK_DIGIT)
                    .build();

            InvalidCardNumberException exception = assertThrows(InvalidCardNumberException.class,
                    () -> stolenCardAdminService.addStolenCardNumber(requestWithInvalidCardNumber));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertTrue(exception.getMessage().contains(CARD_NUMBER_WITH_INVALID_CHECK_DIGIT));
            Mockito.verifyNoInteractions(stolenCardService);
        }

        @Test
        @DisplayName("Should propagate InvalidCardNumberException without touching the service when the card number field is null")
        void shouldPropagateInvalidCardNumberException_whenCardNumberFieldIsNull() {
            StolenCardNumberRequestDto requestWithNullCardNumber = StolenCardNumberRequestDto.builder()
                    .cardNumber(NULL_CARD_NUMBER)
                    .build();

            assertThrows(InvalidCardNumberException.class,
                    () -> stolenCardAdminService.addStolenCardNumber(requestWithNullCardNumber));

            Mockito.verifyNoInteractions(stolenCardService);
        }

        @Test
        @DisplayName("Should propagate InvalidCardNumberException without touching the service when the card number field is empty")
        void shouldPropagateInvalidCardNumberException_whenCardNumberFieldIsEmpty() {
            StolenCardNumberRequestDto requestWithEmptyCardNumber = StolenCardNumberRequestDto.builder()
                    .cardNumber(EMPTY_CARD_NUMBER)
                    .build();

            assertThrows(InvalidCardNumberException.class,
                    () -> stolenCardAdminService.addStolenCardNumber(requestWithEmptyCardNumber));

            Mockito.verifyNoInteractions(stolenCardService);
        }

        @Test
        @DisplayName("Should propagate StolenCardConflictException without converting a response when the card already exists")
        void shouldPropagateStolenCardConflictException_whenCardAlreadyExists() {
            StolenCardNumberRequestDto requestWithValidCardNumber = StolenCardNumberRequestDto.builder()
                    .cardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT)
                    .build();

            Mockito.when(stolenCardService.addCard(requestWithValidCardNumber))
                    .thenThrow(new StolenCardConflictException(CARD_NUMBER_WITH_VALID_CHECK_DIGIT + " already exists in the stolen card list!"));

            StolenCardConflictException exception = assertThrows(StolenCardConflictException.class,
                    () -> stolenCardAdminService.addStolenCardNumber(requestWithValidCardNumber));

            assertEquals(HttpStatus.CONFLICT, exception.getStatus());
            Mockito.verify(stolenCardService, Mockito.never()).convertToDto(Mockito.any());
        }
    }

    @Nested
    @DisplayName("deleteStolenCardNumber method")
    class DeleteStolenCardNumberTest {

        @Test
        @DisplayName("Should delete the card and return a status message naming it when the card number is not null")
        void shouldDeleteCardAndReturnStatusMessage_whenCardNumberIsNotNull() {
            StatusResponseDto actualResponse = stolenCardAdminService.deleteStolenCardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT);

            assertEquals("Card " + CARD_NUMBER_WITH_VALID_CHECK_DIGIT + " successfully removed!", actualResponse.getStatus());
            Mockito.verify(stolenCardService).deleteCard(CARD_NUMBER_WITH_VALID_CHECK_DIGIT);
        }

        @Test
        @DisplayName("Should throw InvalidCardNumberException without touching the service when the card number is null")
        void shouldThrowInvalidCardNumberException_whenCardNumberIsNull() {
            InvalidCardNumberException exception = assertThrows(InvalidCardNumberException.class,
                    () -> stolenCardAdminService.deleteStolenCardNumber(NULL_CARD_NUMBER));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            Mockito.verifyNoInteractions(stolenCardService);
        }

        @Test
        @DisplayName("Should throw InvalidCardNumberException without touching service when the card number is empty")
        void shouldDelegateToServiceAndBuildStatusMessage_whenCardNumberIsEmpty() {

            InvalidCardNumberException exception = assertThrows(InvalidCardNumberException.class,
                    () -> stolenCardAdminService.deleteStolenCardNumber(EMPTY_CARD_NUMBER));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            Mockito.verifyNoInteractions(stolenCardService);
        }

        @Test
        @DisplayName("Should propagate StolenCardNumberNotFoundException without building a status message when the card does not exist")
        void shouldPropagateStolenCardNumberNotFoundException_whenCardDoesNotExist() {
            Mockito.doThrow(new StolenCardNumberNotFoundException(CARD_NUMBER_WITH_VALID_CHECK_DIGIT + " not found!"))
                    .when(stolenCardService).deleteCard(CARD_NUMBER_WITH_VALID_CHECK_DIGIT);

            StolenCardNumberNotFoundException exception = assertThrows(StolenCardNumberNotFoundException.class,
                    () -> stolenCardAdminService.deleteStolenCardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        }
    }
}
