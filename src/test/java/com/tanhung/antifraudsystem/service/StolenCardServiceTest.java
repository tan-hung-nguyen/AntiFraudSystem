package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.StolenCardNumberRequestDto;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponseDto;
import com.tanhung.antifraudsystem.exception.StolenCardConflictException;
import com.tanhung.antifraudsystem.exception.StolenCardNumberNotFoundException;
import com.tanhung.antifraudsystem.mapper.StolenCardMapper;
import com.tanhung.antifraudsystem.mapper.StolenCardMapperImpl;
import com.tanhung.antifraudsystem.model.StolenCard;
import com.tanhung.antifraudsystem.repo.StolenCardRepo;
import com.tanhung.antifraudsystem.validators.StolenCardValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StolenCardServiceTest {

    private static final String FIRST_CARD_NUMBER = "4000008449433403";
    private static final String SECOND_CARD_NUMBER = "5100008449433412";

    private static final Long FIRST_STOLEN_CARD_ID = 1L;
    private static final Long SECOND_STOLEN_CARD_ID = 2L;

    private static final String CONFLICT_MESSAGE_SUFFIX = " already exists in the stolen card list!";
    private static final String NOT_FOUND_MESSAGE_SUFFIX = " not found!";

    @Mock
    private StolenCardRepo stolenCardRepo;

    @Mock
    private StolenCardValidator stolenCardValidator;

    private final StolenCardMapper stolenCardMapper = new StolenCardMapperImpl();

    private StolenCardService stolenCardService;

    @BeforeEach
    void setUpStolenCardService() {
        stolenCardService = new StolenCardService(stolenCardMapper, stolenCardRepo, stolenCardValidator);
    }

    @Nested
    @DisplayName("addCard() method")
    class AddCardMethodTest {

        @Test
        @DisplayName("Should map the request to an entity and save it when the card number is not already marked as stolen")
        void shouldMapRequestAndSaveEntity_whenCardNumberIsNotAlreadyStolen() {
            StolenCardNumberRequestDto requestDto = StolenCardNumberRequestDto.builder()
                    .cardNumber(FIRST_CARD_NUMBER)
                    .build();
            StolenCard savedCard = StolenCard.builder()
                    .id(FIRST_STOLEN_CARD_ID)
                    .cardNumber(FIRST_CARD_NUMBER)
                    .build();

            Mockito.when(stolenCardValidator.isStolenCard(FIRST_CARD_NUMBER)).thenReturn(false);
            Mockito.when(stolenCardRepo.save(Mockito.any(StolenCard.class))).thenReturn(savedCard);

            StolenCard actualCard = stolenCardService.addCard(requestDto);

            ArgumentCaptor<StolenCard> savedCardCaptor = ArgumentCaptor.forClass(StolenCard.class);
            Mockito.verify(stolenCardRepo).save(savedCardCaptor.capture());
            assertEquals(FIRST_CARD_NUMBER, savedCardCaptor.getValue().getCardNumber());
            assertSame(savedCard, actualCard);
        }

        @Test
        @DisplayName("Should throw StolenCardConflictException naming the card number without saving " +
                "when the card number is already marked as stolen")
        void shouldThrowStolenCardConflictException_whenCardNumberIsAlreadyStolen() {
            StolenCardNumberRequestDto requestDto = StolenCardNumberRequestDto.builder()
                    .cardNumber(FIRST_CARD_NUMBER)
                    .build();

            Mockito.when(stolenCardValidator.isStolenCard(FIRST_CARD_NUMBER)).thenReturn(true);

            StolenCardConflictException exception = assertThrows(StolenCardConflictException.class,
                    () -> stolenCardService.addCard(requestDto));

            assertEquals(HttpStatus.CONFLICT, exception.getStatus());
            assertTrue(exception.getMessage().contains(FIRST_CARD_NUMBER));
            assertTrue(exception.getMessage().endsWith(CONFLICT_MESSAGE_SUFFIX));
            Mockito.verify(stolenCardRepo, Mockito.never()).save(Mockito.any());
        }
    }

    @Nested
    @DisplayName("deleteCard() method")
    class DeleteCardMethodTest {

        @Test
        @DisplayName("Should throw StolenCardNumberNotFoundException naming the card number without deleting " +
                "when the validator reports the card number as stolen")
        void shouldThrowStolenCardNumberNotFoundException_whenValidatorReportsCardNumberAsStolen() {
            Mockito.when(stolenCardValidator.isStolenCard(FIRST_CARD_NUMBER)).thenReturn(false);

            StolenCardNumberNotFoundException exception = assertThrows(StolenCardNumberNotFoundException.class,
                    () -> stolenCardService.deleteCard(FIRST_CARD_NUMBER));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertTrue(exception.getMessage().contains(FIRST_CARD_NUMBER));
            assertTrue(exception.getMessage().endsWith(NOT_FOUND_MESSAGE_SUFFIX));
            Mockito.verify(stolenCardRepo, Mockito.never()).deleteStolenCardByCardNumber(Mockito.anyString());
        }

        @Test
        @DisplayName("Should delete the card by number when the validator reports the card number as not stolen")
        void shouldDeleteCardByNumber_whenValidatorReportsCardNumberAsNotStolen() {
            Mockito.when(stolenCardValidator.isStolenCard(FIRST_CARD_NUMBER)).thenReturn(true);

            stolenCardService.deleteCard(FIRST_CARD_NUMBER);

            Mockito.verify(stolenCardRepo).deleteStolenCardByCardNumber(FIRST_CARD_NUMBER);
        }
    }

    @Nested
    @DisplayName("convertToDto() method")
    class ConvertToDtoMethodTest {

        @Test
        @DisplayName("Should map every field from the entity to the response dto when the entity is not null")
        void shouldMapEveryField_whenEntityIsNotNull() {
            StolenCard stolenCard = StolenCard.builder()
                    .id(FIRST_STOLEN_CARD_ID)
                    .cardNumber(FIRST_CARD_NUMBER)
                    .build();

            StolenCardResponseDto actualResponse = stolenCardService.convertToDto(stolenCard);

            assertEquals(FIRST_STOLEN_CARD_ID, actualResponse.getId());
            assertEquals(FIRST_CARD_NUMBER, actualResponse.getCardNumber());
        }

        @Test
        @DisplayName("Should return null when the entity is null")
        void shouldReturnNull_whenEntityIsNull() {
            StolenCardResponseDto actualResponse = stolenCardService.convertToDto(null);

            assertNull(actualResponse);
        }
    }

    @Nested
    @DisplayName("getAllStolenCards() method")
    class GetAllStolenCardsMethodTest {

        @Test
        @DisplayName("Should return every stolen card mapped to a response dto sorted by id when the repository has records")
        void shouldReturnAllStolenCardsMappedToResponseDto_whenRepositoryHasRecords() {
            StolenCard firstStolenCard = StolenCard.builder()
                    .id(FIRST_STOLEN_CARD_ID)
                    .cardNumber(FIRST_CARD_NUMBER)
                    .build();
            StolenCard secondStolenCard = StolenCard.builder()
                    .id(SECOND_STOLEN_CARD_ID)
                    .cardNumber(SECOND_CARD_NUMBER)
                    .build();

            Mockito.when(stolenCardRepo.findAll(Sort.by("id")))
                    .thenReturn(List.of(firstStolenCard, secondStolenCard));

            List<StolenCardResponseDto> actualStolenCards = stolenCardService.getAllStolenCards();

            assertEquals(2, actualStolenCards.size());
            assertEquals(FIRST_STOLEN_CARD_ID, actualStolenCards.get(0).getId());
            assertEquals(FIRST_CARD_NUMBER, actualStolenCards.get(0).getCardNumber());
            assertEquals(SECOND_STOLEN_CARD_ID, actualStolenCards.get(1).getId());
            assertEquals(SECOND_CARD_NUMBER, actualStolenCards.get(1).getCardNumber());
        }

        @Test
        @DisplayName("Should return an empty list when the repository has no stolen cards on record")
        void shouldReturnEmptyList_whenRepositoryHasNoStolenCards() {
            Mockito.when(stolenCardRepo.findAll(Sort.by("id"))).thenReturn(Collections.emptyList());

            List<StolenCardResponseDto> actualStolenCards = stolenCardService.getAllStolenCards();

            assertTrue(actualStolenCards.isEmpty());
        }
    }
}