package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequestDto;
import com.tanhung.antifraudsystem.dto.response.IPResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.exception.IPAddressNullException;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
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
class SuspiciousIpAdminServiceTest {

    private static final String FIRST_IP_ADDRESS = "192.168.1.100";
    private static final SuspiciousIpRequestDto NULL_REQUEST_DTO = null;
    private static final String NULL_IP_ADDRESS = null;

    private static final Long SUSPICIOUS_IP_ID = 1L;

    private static final String NULL_REQUEST_MESSAGE = "IP object cannot be null!";
    private static final String NULL_IP_MESSAGE = "IP address must not be null!";
    private static final String DELETE_SUCCESS_MESSAGE_PREFIX = "IP ";
    private static final String DELETE_SUCCESS_MESSAGE_SUFFIX = " successfully removed!";

    @Mock
    private SuspiciousIpService suspiciousIpService;

    @InjectMocks
    private SuspiciousIpAdminService suspiciousIpAdminService;

    @Nested
    @DisplayName("addIP() method")
    class AddIpMethodTest {

        @Test
        @DisplayName("Should add the ip address and return its response dto when the request is not null")
        void shouldAddIpAndReturnResponseDto_whenRequestIsNotNull() {
            SuspiciousIpRequestDto requestDto = SuspiciousIpRequestDto.builder()
                    .ipAddress(FIRST_IP_ADDRESS)
                    .build();
            SuspiciousIPAddress savedIpEntity = SuspiciousIPAddress.builder()
                    .id(SUSPICIOUS_IP_ID)
                    .ipAddress(FIRST_IP_ADDRESS)
                    .build();
            IPResponseDto expectedResponse = IPResponseDto.builder()
                    .id(SUSPICIOUS_IP_ID)
                    .ipAddress(FIRST_IP_ADDRESS)
                    .build();

            Mockito.when(suspiciousIpService.addIp(requestDto)).thenReturn(savedIpEntity);
            Mockito.when(suspiciousIpService.convertToDto(savedIpEntity)).thenReturn(expectedResponse);

            IPResponseDto actualResponse = suspiciousIpAdminService.addIP(requestDto);

            assertSame(expectedResponse, actualResponse);
            Mockito.verify(suspiciousIpService).addIp(requestDto);
            Mockito.verify(suspiciousIpService).convertToDto(savedIpEntity);
        }

        @Test
        @DisplayName("Should throw IPAddressNullException without touching the service when the request is null")
        void shouldThrowIPAddressNullException_whenRequestIsNull() {
            IPAddressNullException exception = assertThrows(IPAddressNullException.class,
                    () -> suspiciousIpAdminService.addIP(NULL_REQUEST_DTO));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals(NULL_REQUEST_MESSAGE, exception.getMessage());
            Mockito.verifyNoInteractions(suspiciousIpService);
        }

    }

    @Nested
    @DisplayName("deleteIP() method")
    class DeleteIpMethodTest {

        @Test
        @DisplayName("Should delete the ip address and return a status message naming it when the ip address is not null")
        void shouldDeleteIpAndReturnStatusMessage_whenIpAddressIsNotNull() {
            StatusResponseDto actualResponse = suspiciousIpAdminService.deleteIP(FIRST_IP_ADDRESS);

            assertEquals(DELETE_SUCCESS_MESSAGE_PREFIX + FIRST_IP_ADDRESS + DELETE_SUCCESS_MESSAGE_SUFFIX,
                    actualResponse.getStatus());
            Mockito.verify(suspiciousIpService).deleteIp(FIRST_IP_ADDRESS);
        }

        @Test
        @DisplayName("Should throw IPAddressNullException without touching the service when the ip address is null")
        void shouldThrowIPAddressNullException_whenIpAddressIsNull() {
            IPAddressNullException exception = assertThrows(IPAddressNullException.class,
                    () -> suspiciousIpAdminService.deleteIP(NULL_IP_ADDRESS));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals(NULL_IP_MESSAGE, exception.getMessage());
            Mockito.verifyNoInteractions(suspiciousIpService);
        }

    }
}