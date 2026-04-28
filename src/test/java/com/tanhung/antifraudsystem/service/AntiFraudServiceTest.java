package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequest;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.IPResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.mapper.StolenCardMapper;
import com.tanhung.antifraudsystem.mapper.StolenCardMapperImpl;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapper;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapperImpl;
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
        private final SuspiciousIpRequest request = SuspiciousIpRequest
                                                .builder()
                                                .ipAddress("192.168.1.1").build();
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

            assertEquals(2, actual.size());
            assertEquals(1,actual.getFirst().getId());
            assertEquals(2, actual.get(1).getId());
        }

        @Test
        @DisplayName("Should return an empty list of IPResponse when the list is empty")
        void shouldReturnEmptyListOfIPResponse_whenIPListIsEmpty() {
            Mockito.when(suspiciousIPRepo.findAll(Sort.by("id")))
                    .thenReturn(List.of());

            List<IPResponse> actual = antiFraudService.getAllSuspiciousIPs();
            assertTrue(actual.isEmpty());
        }
    }

}