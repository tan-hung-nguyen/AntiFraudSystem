package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequestDto;
import com.tanhung.antifraudsystem.dto.response.IPResponseDto;
import com.tanhung.antifraudsystem.exception.IPAddressConflictException;
import com.tanhung.antifraudsystem.exception.IPAddressNotFoundException;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapper;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapperImpl;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.repo.SuspiciousIPRepo;
import com.tanhung.antifraudsystem.validators.SuspiciousIpValidator;
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
class SuspiciousIpServiceTest {

    private static final String FIRST_IP_ADDRESS = "192.168.1.100";
    private static final String SECOND_IP_ADDRESS = "10.0.0.5";

    private static final Long FIRST_SUSPICIOUS_IP_ID = 1L;
    private static final Long SECOND_SUSPICIOUS_IP_ID = 2L;

    private static final String CONFLICT_MESSAGE_SUFFIX = " already exists in the suspicious ip list!";
    private static final String NOT_FOUND_MESSAGE_SUFFIX = " not found!";

    @Mock
    private SuspiciousIPRepo suspiciousIPRepo;

    @Mock
    private SuspiciousIpValidator suspiciousIpValidator;

    private final SuspiciousIpAddressMapper suspiciousIpAddressMapper = new SuspiciousIpAddressMapperImpl();

    private SuspiciousIpService suspiciousIpService;

    @BeforeEach
    void setUpSuspiciousIpService() {
        suspiciousIpService = new SuspiciousIpService(suspiciousIPRepo, suspiciousIpAddressMapper, suspiciousIpValidator);
    }

    @Nested
    @DisplayName("addIp() method")
    class AddIpMethodTest {

        @Test
        @DisplayName("Should map the request to an entity and save it when the ip address is not already marked as suspicious")
        void shouldMapRequestAndSaveEntity_whenIpAddressIsNotAlreadySuspicious() {
            SuspiciousIpRequestDto requestDto = SuspiciousIpRequestDto.builder()
                    .ipAddress(FIRST_IP_ADDRESS)
                    .build();
            SuspiciousIPAddress savedIpAddress = SuspiciousIPAddress.builder()
                    .id(FIRST_SUSPICIOUS_IP_ID)
                    .ipAddress(FIRST_IP_ADDRESS)
                    .build();

            Mockito.when(suspiciousIpValidator.isSuspiciousIp(FIRST_IP_ADDRESS)).thenReturn(false);
            Mockito.when(suspiciousIPRepo.save(Mockito.any(SuspiciousIPAddress.class))).thenReturn(savedIpAddress);

            SuspiciousIPAddress actualIpAddress = suspiciousIpService.addIp(requestDto);

            ArgumentCaptor<SuspiciousIPAddress> savedIpCaptor = ArgumentCaptor.forClass(SuspiciousIPAddress.class);
            Mockito.verify(suspiciousIPRepo).save(savedIpCaptor.capture());
            assertEquals(FIRST_IP_ADDRESS, savedIpCaptor.getValue().getIpAddress());
            assertSame(savedIpAddress, actualIpAddress);
        }

        @Test
        @DisplayName("Should throw IPAddressConflictException naming the ip address without saving " +
                "when the ip address is already marked as suspicious")
        void shouldThrowIPAddressConflictException_whenIpAddressIsAlreadySuspicious() {
            SuspiciousIpRequestDto requestDto = SuspiciousIpRequestDto.builder()
                    .ipAddress(FIRST_IP_ADDRESS)
                    .build();

            Mockito.when(suspiciousIpValidator.isSuspiciousIp(FIRST_IP_ADDRESS)).thenReturn(true);

            IPAddressConflictException exception = assertThrows(IPAddressConflictException.class,
                    () -> suspiciousIpService.addIp(requestDto));

            assertEquals(HttpStatus.CONFLICT, exception.getStatus());
            assertTrue(exception.getMessage().contains(FIRST_IP_ADDRESS));
            assertTrue(exception.getMessage().endsWith(CONFLICT_MESSAGE_SUFFIX));
            Mockito.verify(suspiciousIPRepo, Mockito.never()).save(Mockito.any());
        }
    }

    @Nested
    @DisplayName("deleteIp() method")
    class DeleteIpMethodTest {

        @Test
        @DisplayName("Should throw IPAddressNotFoundException naming the ip address without deleting " +
                "when the validator reports the ip address as not suspicious")
        void shouldThrowIPAddressNotFoundException_whenValidatorReportsIpAddressAsNotSuspicious() {
            Mockito.when(suspiciousIpValidator.isSuspiciousIp(FIRST_IP_ADDRESS)).thenReturn(false);

            IPAddressNotFoundException exception = assertThrows(IPAddressNotFoundException.class,
                    () -> suspiciousIpService.deleteIp(FIRST_IP_ADDRESS));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertTrue(exception.getMessage().contains(FIRST_IP_ADDRESS));
            assertTrue(exception.getMessage().endsWith(NOT_FOUND_MESSAGE_SUFFIX));
            Mockito.verify(suspiciousIPRepo, Mockito.never()).deleteByIpAddress(Mockito.anyString());
        }

        @Test
        @DisplayName("Should delete the ip address when the validator reports the ip address as suspicious")
        void shouldDeleteIpAddress_whenValidatorReportsIpAddressAsSuspicious() {
            Mockito.when(suspiciousIpValidator.isSuspiciousIp(FIRST_IP_ADDRESS)).thenReturn(true);

            suspiciousIpService.deleteIp(FIRST_IP_ADDRESS);

            Mockito.verify(suspiciousIPRepo).deleteByIpAddress(FIRST_IP_ADDRESS);
        }
    }

    @Nested
    @DisplayName("convertToDto() method")
    class ConvertToDtoMethodTest {

        @Test
        @DisplayName("Should map every field from the entity to the response dto when the entity is not null")
        void shouldMapEveryField_whenEntityIsNotNull() {
            SuspiciousIPAddress ipAddress = SuspiciousIPAddress.builder()
                    .id(FIRST_SUSPICIOUS_IP_ID)
                    .ipAddress(FIRST_IP_ADDRESS)
                    .build();

            IPResponseDto actualResponse = suspiciousIpService.convertToDto(ipAddress);

            assertEquals(FIRST_SUSPICIOUS_IP_ID, actualResponse.getId());
            assertEquals(FIRST_IP_ADDRESS, actualResponse.getIpAddress());
        }

        @Test
        @DisplayName("Should return null when the entity is null")
        void shouldReturnNull_whenEntityIsNull() {
            IPResponseDto actualResponse = suspiciousIpService.convertToDto(null);

            assertNull(actualResponse);
        }
    }

    @Nested
    @DisplayName("getAllSuspiciousIps() method")
    class GetAllSuspiciousIpsMethodTest {

        @Test
        @DisplayName("Should return every suspicious ip mapped to a response dto sorted by id when the repository has records")
        void shouldReturnAllSuspiciousIpsMappedToResponseDto_whenRepositoryHasRecords() {
            SuspiciousIPAddress firstSuspiciousIp = SuspiciousIPAddress.builder()
                    .id(FIRST_SUSPICIOUS_IP_ID)
                    .ipAddress(FIRST_IP_ADDRESS)
                    .build();
            SuspiciousIPAddress secondSuspiciousIp = SuspiciousIPAddress.builder()
                    .id(SECOND_SUSPICIOUS_IP_ID)
                    .ipAddress(SECOND_IP_ADDRESS)
                    .build();

            Mockito.when(suspiciousIPRepo.findAll(Sort.by("id")))
                    .thenReturn(List.of(firstSuspiciousIp, secondSuspiciousIp));

            List<IPResponseDto> actualSuspiciousIps = suspiciousIpService.getAllSuspiciousIps();

            assertEquals(2, actualSuspiciousIps.size());
            assertEquals(FIRST_SUSPICIOUS_IP_ID, actualSuspiciousIps.get(0).getId());
            assertEquals(FIRST_IP_ADDRESS, actualSuspiciousIps.get(0).getIpAddress());
            assertEquals(SECOND_SUSPICIOUS_IP_ID, actualSuspiciousIps.get(1).getId());
            assertEquals(SECOND_IP_ADDRESS, actualSuspiciousIps.get(1).getIpAddress());
        }

        @Test
        @DisplayName("Should return an empty list when the repository has no suspicious ips on record")
        void shouldReturnEmptyList_whenRepositoryHasNoSuspiciousIps() {
            Mockito.when(suspiciousIPRepo.findAll(Sort.by("id"))).thenReturn(Collections.emptyList());

            List<IPResponseDto> actualSuspiciousIps = suspiciousIpService.getAllSuspiciousIps();

            assertTrue(actualSuspiciousIps.isEmpty());
        }
    }
}