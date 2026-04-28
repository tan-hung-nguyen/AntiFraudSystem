package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.StolenCardRequest;
import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequest;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.IPResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponse;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.mapper.StolenCardMapper;
import com.tanhung.antifraudsystem.mapper.StolenCardMapperImpl;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapper;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapperImpl;
import com.tanhung.antifraudsystem.model.StolenCard;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.repo.StolenCardRepo;
import com.tanhung.antifraudsystem.repo.SuspiciousIPRepo;
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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class AntiFraudServiceTest {

    @Mock
    private SuspiciousIPRepo suspiciousIPRepo;
    @Mock
    private StolenCardRepo stolenCardRepo;
    private final SuspiciousIpAddressMapper ipAddressMapper = new SuspiciousIpAddressMapperImpl();
    private final StolenCardMapper stolenCardMapper = new StolenCardMapperImpl();

    private AntiFraudService antiFraudService;

    @BeforeEach
    void setUp() {
        antiFraudService = new AntiFraudService(suspiciousIPRepo,
                                                stolenCardRepo,
                                                ipAddressMapper,
                                                stolenCardMapper);
    }
    @Nested
    @DisplayName("addIP method")
    class addIPTest{
        private SuspiciousIpRequest request;
        @BeforeEach
        void setUpRequest()
        {
          request = SuspiciousIpRequest
                    .builder()
                    .ipAddress("192.168.1.1").build();
        }
        @Test
        @DisplayName("Should return IPResponse dto when add successfully")
        void shouldReturnIPResponseDto_whenAddSuccessfully() {

            Mockito.when(suspiciousIPRepo.existsByIp(Mockito.eq("192.168.1.1")))
                    .thenReturn(false);
            Mockito.when(suspiciousIPRepo.save(Mockito.any()))
                    .thenReturn(new SuspiciousIPAddress(1L, "192.168.1.1"));

            IPResponse actual = antiFraudService.addIP(request);

            assertEquals(request.getIpAddress(), actual.getIpAddress());
            assertEquals(1, actual.getId());
            Mockito.verify(suspiciousIPRepo).save(Mockito.any());


        }

        @Test
        @DisplayName("Should throw IPAddressNullException with bad request status 400 " +
                "when ip address is null")
        void shouldThrowIPAddressNullException_whenIPAddressIsNull() {
            IPAddressException ex = assertThrows(IPAddressException.class,
                        () -> antiFraudService.addIP(null));
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
            Mockito.verify(suspiciousIPRepo, Mockito.never()).existsByIp(Mockito.any());
            Mockito.verify(suspiciousIPRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Should throw IPAddressConflictException with conflict status 409 " +
                "when ip address is already in the list")
        void shouldThrowIPAddressConflictException_whenIpAddressIsAlreadyInTheList() {
            Mockito.when(suspiciousIPRepo.existsByIp(Mockito.eq("192.168.1.1")))
                    .thenReturn(true);

            IPAddressConflictException ex = assertThrows(IPAddressConflictException.class,
                                                () -> antiFraudService.addIP(request));

            assertEquals("Conflict", ex.getStatus().getReasonPhrase());
            Mockito.verify(suspiciousIPRepo, Mockito.never()).save(Mockito.any());
        }
    }

    @Nested
    @DisplayName("deleteIP method")
    class deleteIPTest{
        private final String ipAddress = "192.168.1.1";
        @Test
        @DisplayName("Should return StatusResponse when deleting successfully")
        void shouldReturnStatusResponse_whenDeletingSuccessfully() {
            Mockito.when(suspiciousIPRepo.existsByIp(Mockito.eq(ipAddress)))
                    .thenReturn(true);

            StatusResponse actual = antiFraudService.deleteIP(ipAddress);

            assertNotNull(actual);
            assertNotNull(actual.getStatus());
            assertEquals("IP 192.168.1.1 successfully removed!", actual.getStatus());
            Mockito.verify(suspiciousIPRepo).deleteByIp(Mockito.any());

        }

        @Test
        @DisplayName("Should throw IPAddressNullException with bad request status 400 when ip address is null")
        void shouldThrowIPAddressNullException_whenIPAddressIsNull() {
            IPAddressNullException ex = assertThrows(IPAddressNullException.class,
                    () -> antiFraudService.deleteIP(null));

            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
            Mockito.verify(suspiciousIPRepo, Mockito.never()).existsByIp(Mockito.any());
            Mockito.verify(suspiciousIPRepo, Mockito.never()).deleteByIp(Mockito.any());

        }

        @Test
        @DisplayName("Should throw IPAddressNotFoundException with not found status 404 when ip address not found")
        void shouldThrowIPAddressNotFoundException_whenIPAddressNotFound() {
            IPAddressNotFoundException ex = assertThrows(IPAddressNotFoundException.class,
                    () -> antiFraudService.deleteIP(ipAddress));

            assertEquals("Not Found", ex.getStatus().getReasonPhrase());
            Mockito.verify(suspiciousIPRepo, Mockito.never()).deleteByIp(ipAddress);

        }
    }

    @Nested
    @DisplayName("getAllSuspiciousIPs Method")
    class getAllSuspiciousIPsTest{
        @Test
        @DisplayName("Should return a list of IPResponse ordered by id when the list is not empty")
        void shouldReturnListOfIPResponse_whenIPListIsNotEmpty() {
            Mockito.when(suspiciousIPRepo.findAll(Sort.by("id")))
                        .thenReturn(List.of(new SuspiciousIPAddress(1L, "test"),
                                            new SuspiciousIPAddress(2L, "test2")));

            List<IPResponse> actual = antiFraudService.getAllSuspiciousIPs();

            assertFalse(actual.isEmpty());
            assertEquals(2, actual.size());
            assertEquals(1,actual.getFirst().getId());
            assertEquals(2, actual.get(1).getId());
            Mockito.verify(suspiciousIPRepo).findAll(Sort.by("id"));
        }

        @Test
        @DisplayName("Should return an empty list of IPResponse when the list is empty")
        void shouldReturnEmptyListOfIPResponse_whenIPListIsEmpty() {
            Mockito.when(suspiciousIPRepo.findAll(Sort.by("id")))
                    .thenReturn(List.of());

            List<IPResponse> actual = antiFraudService.getAllSuspiciousIPs();
            assertTrue(actual.isEmpty());
            assertNotNull(actual);
            Mockito.verify(suspiciousIPRepo).findAll(Sort.by("id"));
        }
    }

    @Nested
    @DisplayName("addStolenCardNumber Method")
    class addStolenCardNumberTest{
        private StolenCardRequest request;

        @BeforeEach
        void setUpRequest() {
            request = StolenCardRequest.builder()
                    .cardNumber("4000008449433403")
                    .build();
        }

        @Test
        @DisplayName("Should return StolenCardResponse with id, card number when card number adding successfully")
        void shouldReturnStolenCardResponse_whenCardNumberAddSuccessfully() {
            Mockito.when(stolenCardRepo.existsStolenCardByCardNumber(Mockito.eq("4000008449433403")))
                    .thenReturn(false);
            Mockito.when(stolenCardRepo.save(Mockito.any()))
                    .thenReturn(new StolenCard(1L, "4000008449433403"));

            StolenCardResponse actual = antiFraudService.addStolenCardNumber(request);

            assertEquals(1, actual.getId());
            assertEquals("4000008449433403", actual.getCardNumber());
            Mockito.verify(stolenCardRepo).save(Mockito.any());

        }

        @Test
        @DisplayName("Should throw StolenCardNullException with bad request status 400 when card number is null")
        void shouldThrowStolenCardNullException_whenCardNumberIsNull(){

            StolenCardNullException ex = assertThrows(StolenCardNullException.class,
                    () -> antiFraudService.addStolenCardNumber(null));
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
            Mockito.verify(stolenCardRepo, Mockito.never()).existsStolenCardByCardNumber(Mockito.any());
            Mockito.verify(stolenCardRepo, Mockito.never()).save(Mockito.any());

        }

        @Test
        @DisplayName("Should throw StolenCardConflictException with conflict status 409 when " +
                "card number is already in the list")
        void shouldThrowStolenCardConflictException_whenCardNumberIsAlreadyInTheList() {
            Mockito.when(stolenCardRepo.existsStolenCardByCardNumber(Mockito.eq("4000008449433403")))
                    .thenReturn(true);

            StolenCardConflictException ex = assertThrows(StolenCardConflictException.class,
                    () -> antiFraudService.addStolenCardNumber(request));
            assertEquals("Conflict", ex.getStatus().getReasonPhrase());
            Mockito.verify(stolenCardRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Should throw InvalidCardNumberException with bad request status 400 when " +
                "card number is invalid")
        void shouldThrowInvalidCardNumber_whenCardNumberIsInvalid() {
            request.setCardNumber("4000008449433408");
            InvalidCardNumberException ex = assertThrows(InvalidCardNumberException.class,
                    () -> antiFraudService.addStolenCardNumber(request));
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
            Mockito.verify(stolenCardRepo, Mockito.never()).existsStolenCardByCardNumber(Mockito.any());
            Mockito.verify(stolenCardRepo, Mockito.never()).save(Mockito.any());
        }
    }

    @Nested
    @DisplayName("deleteStolenCardNumber Method")
    class deleteStolenCardNumberTest{
        private final String cardNumber = "4000008449433403";

        @Test
        @DisplayName("Should return StatusResponse when deleting card number successfully")
        void shouldReturnStatusResponse_whenDeletingCardNumberSuccessfully() {
            Mockito.when(stolenCardRepo.existsStolenCardByCardNumber(cardNumber))
                    .thenReturn(true);
            StatusResponse actual = antiFraudService.deleteStolenCardNumber(cardNumber);

            assertNotNull(actual);
            assertNotNull(actual.getStatus());
            assertEquals("Card 4000008449433403 successfully removed!", actual.getStatus());
            Mockito.verify(stolenCardRepo).deleteStolenCardByCardNumber(cardNumber);
        }

        @Test
        @DisplayName("Should throw StolenCardNullException with bad request status 400 when card number is null")
        void shouldThrowStolenCardNumberNullException_whenCardNumberIsNull(){
            StolenCardNullException ex = assertThrows(StolenCardNullException.class,
                    () -> antiFraudService.deleteStolenCardNumber(null));
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
            Mockito.verify(stolenCardRepo, Mockito.never()).existsStolenCardByCardNumber(cardNumber);
            Mockito.verify(stolenCardRepo, Mockito.never()).deleteStolenCardByCardNumber(cardNumber);
        }

        @Test
        @DisplayName("Should throw StolenCardNotFoundException with not found status 404 when card number not found")
        void shouldThrowStolenCardNumberNotFoundException_whenCardNumberNotFound(){
            Mockito.when(stolenCardRepo.existsStolenCardByCardNumber(cardNumber))
                    .thenReturn(false);
            StolenCardNumberNotFoundException ex = assertThrows(StolenCardNumberNotFoundException.class,
                    () -> antiFraudService.deleteStolenCardNumber(cardNumber));
            assertEquals("Not Found", ex.getStatus().getReasonPhrase());
            Mockito.verify(stolenCardRepo, Mockito.never()).deleteStolenCardByCardNumber(cardNumber);
        }
    }

    @Nested
    @DisplayName("getAllStolenCards Method")
    class getAllStolenCardsTest{

        @Test
        @DisplayName("Should return list of StolenCardResponse with id, card number when stolen card list is not empty")
        void shouldReturnListOfStolenCardResponse_whenListIsNotEmpty() {
            Mockito.when(stolenCardRepo.findAll(Sort.by("id")))
                    .thenReturn(List.of(new StolenCard(1L, "Test"),
                            new StolenCard(2L, "Test")));
            List<StolenCardResponse> actual = antiFraudService.getAllStolenCards();

            assertFalse(actual.isEmpty());
            assertEquals(2, actual.size());
            assertEquals(1, actual.getFirst().getId());
            assertEquals(2, actual.get(1).getId());

            Mockito.verify(stolenCardRepo).findAll(Sort.by("id"));
        }

        @Test
        @DisplayName("Should return empty list of StolenCardResponse when stolen card list is empty")
        void shouldReturnEmptyListOfStolenCardResponse_whenListIsEmpty() {
            Mockito.when(stolenCardRepo.findAll(Sort.by("id")))
                    .thenReturn(List.of());
            List<StolenCardResponse> actual = antiFraudService.getAllStolenCards();

            assertTrue(actual.isEmpty());
            assertNotNull(actual);
            Mockito.verify(stolenCardRepo).findAll(Sort.by("id"));
        }
    }

}