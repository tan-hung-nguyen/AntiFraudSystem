package com.tanhung.antifraudsystem.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardNumberValidatorTest {

    private static final String CARD_NUMBER_WITH_VALID_CHECK_DIGIT = "4000008449433403";
    private static final String CARD_NUMBER_WITH_INVALID_CHECK_DIGIT = "4000008449433404";
    private static final String CARD_NUMBER_WITH_NON_DIGIT_CHECK_CHARACTER = "400000844943340A";
    private static final String CARD_NUMBER_TOO_SHORT = "123456789012345";
    private static final String CARD_NUMBER_TOO_LONG = "12345678901234567";
    private static final String BLANK_CARD_NUMBER = "                ";
    private static final String EMPTY_CARD_NUMBER = "";
    private static final String NULL_CARD_NUMBER = null;

    @Test
    @DisplayName("Should return true when the card number is 16 digits and satisfies the check-digit formula")
    void shouldReturnTrue_whenCardNumberSatisfiesCheckDigitFormula() {
        assertTrue(CardNumberValidator.isValidCardNumber(CARD_NUMBER_WITH_VALID_CHECK_DIGIT));
    }

    @Test
    @DisplayName("Should return false when the card number is 16 digits but fails the check-digit formula")
    void shouldReturnFalse_whenCardNumberFailsCheckDigitFormula() {
        assertFalse(CardNumberValidator.isValidCardNumber(CARD_NUMBER_WITH_INVALID_CHECK_DIGIT));
    }

    @Test
    @DisplayName("Should return false when the card number is null")
    void shouldReturnFalse_whenCardNumberIsNull() {
        assertFalse(CardNumberValidator.isValidCardNumber(NULL_CARD_NUMBER));
    }

    @Test
    @DisplayName("Should return false when the card number is an empty string")
    void shouldReturnFalse_whenCardNumberIsEmpty() {
        assertFalse(CardNumberValidator.isValidCardNumber(EMPTY_CARD_NUMBER));
    }

    @Test
    @DisplayName("Should return false when the card number is blank")
    void shouldReturnFalse_whenCardNumberIsBlank() {
        assertFalse(CardNumberValidator.isValidCardNumber(BLANK_CARD_NUMBER));
    }

    @Test
    @DisplayName("Should return false when the card number has fewer than 16 characters")
    void shouldReturnFalse_whenCardNumberIsTooShort() {
        assertFalse(CardNumberValidator.isValidCardNumber(CARD_NUMBER_TOO_SHORT));
    }

    @Test
    @DisplayName("Should return false when the card number has more than 16 characters")
    void shouldReturnFalse_whenCardNumberIsTooLong() {
        assertFalse(CardNumberValidator.isValidCardNumber(CARD_NUMBER_TOO_LONG));
    }

    @Test
    @DisplayName("Should return false when the check-digit character of a 16-character card number is not a digit")
    void shouldReturnFalse_whenCheckDigitCharacterIsNotADigit() {
        assertFalse(CardNumberValidator.isValidCardNumber(CARD_NUMBER_WITH_NON_DIGIT_CHECK_CHARACTER));
    }
}