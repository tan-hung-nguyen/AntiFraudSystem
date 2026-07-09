package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.dto.response.IPResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.exception.IPAddressConflictException;
import com.tanhung.antifraudsystem.exception.IPAddressNotFoundException;
import com.tanhung.antifraudsystem.exception.IPAddressNullException;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.SuspiciousIpAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuspiciousIpController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class SuspiciousIpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SuspiciousIpAdminService suspiciousIpAdminService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("POST /api/antifraud/suspicious-ip")
    class addSuspiciousIpAddressTest {

        @Test
        @DisplayName("Return \"Created 201\" when ip address is in IPV4 format")
        void shouldReturnCreated_whenIpAddressIsValid() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.168.1.1"
                    }
                    """;

            Mockito.when(suspiciousIpAdminService.addIP(Mockito.any()))
                    .thenReturn(new IPResponseDto(1L, "192.168.1.1"));

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.ipAddress").value("192.168.1.1"));

            Mockito.verify(suspiciousIpAdminService).addIP(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress is missing")
        void shouldReturnBadRequest_whenIpAddressIsMissing() throws Exception {
            String json = """
                    {
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress is blank")
        void shouldReturnBadRequest_whenIpAddressIsBlank() throws Exception {
            String json = """
                    {
                        "ipAddress" : ""
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress is missing a dot")
        void shouldReturnBadRequest_whenIpAddressIsMissingDot() throws Exception {
            String json = """
                    {
                        "ipAddress" : "19216811"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress contains letters")
        void shouldReturnBadRequest_whenIpAddressContainsLetters() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.abc.c.1"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress octet is greater than 255")
        void shouldReturnBadRequest_whenIpAddressOctetOutOfRange() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.168.356.1"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress has too few octets")
        void shouldReturnBadRequest_whenIpAddressHasTooFewOctets() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.168.1"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress has too many octets")
        void shouldReturnBadRequest_whenIpAddressHasTooManyOctets() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.168.1.1.1"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress is in IPV6 format")
        void shouldReturnBadRequest_whenIpAddressIsIpv6Format() throws Exception {
            String json = """
                    {
                        "ipAddress" : "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress contains leading/trailing spaces")
        void shouldReturnBadRequest_whenIpAddressContainsSpaces() throws Exception {
            String json = """
                    {
                        "ipAddress" : " 192.168.1.1 "
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request body is empty")
        void shouldReturnBadRequest_whenRequestBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request body is malformed JSON")
        void shouldReturnBadRequest_whenRequestBodyIsMalformedJson() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.168.1.1"
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request contains an unknown field")
        void shouldReturnBadRequest_whenRequestContainsUnknownField() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.168.1.1",
                        "extraField" : "unexpected"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Conflict 409\" when service throws IPAddressConflictException")
        void shouldReturnConflict_whenServiceThrowsIPAddressConflictException() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.168.1.1"
                    }
                    """;

            Mockito.when(suspiciousIpAdminService.addIP(Mockito.any()))
                    .thenThrow(new IPAddressConflictException("192.168.1.1 is existed in the list"));

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /api/antifraud/suspicious-ip/{ip}")
    class deleteSuspiciousIpAddressTest {

        @Test
        @DisplayName("Return \"Ok 200\" when removing an ip from the black list successfully")
        void shouldReturnOk_whenDeletingIpAddressSuccessfully() throws Exception {
            Mockito.when(suspiciousIpAdminService.deleteIP(Mockito.any()))
                    .thenReturn(new StatusResponseDto("IP 192.168.1.1 successfully removed!"));

            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").exists());

            Mockito.verify(suspiciousIpAdminService).deleteIP("192.168.1.1");
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip is missing a dot")
        void shouldReturnBadRequest_whenIpIsMissingDot() throws Exception {
            mockMvc.perform(delete("/api/antifraud/suspicious-ip/19216811")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip contains letters")
        void shouldReturnBadRequest_whenIpContainsLetters() throws Exception {
            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.1ae.w.1")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip octet is greater than 255")
        void shouldReturnBadRequest_whenIpOctetOutOfRange() throws Exception {
            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.356.1")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip has too few octets")
        void shouldReturnBadRequest_whenIpHasTooFewOctets() throws Exception {
            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip is in IPV6 format")
        void shouldReturnBadRequest_whenIpIsIpv6Format() throws Exception {
            mockMvc.perform(delete("/api/antifraud/suspicious-ip/2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Not Found 404\" when path variable is blank")
        void shouldReturnNotFound_whenPathVariableIsBlank() throws Exception {
            mockMvc.perform(delete("/api/antifraud/suspicious-ip/")
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and details when service throws IPAddressNullException")
        void shouldReturnBadRequest_whenServiceThrowsIPAddressNullException() throws Exception {
            Mockito.when(suspiciousIpAdminService.deleteIP(Mockito.any()))
                    .thenThrow(new IPAddressNullException("IP address must not be null!"));

            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Return \"Not Found 404\" and details when service throws IPAddressNotFoundException")
        void shouldReturnNotFound_whenServiceThrowsIPAddressNotFoundException() throws Exception {
            Mockito.when(suspiciousIpAdminService.deleteIP(Mockito.any()))
                    .thenThrow(new IPAddressNotFoundException("IP address not found!"));

            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/antifraud/suspicious-ip")
    class getSuspiciousIpListTest {

        @Test
        @DisplayName("Return \"Ok 200\" and list of suspicious ips when black list is not empty")
        void shouldReturnOkAndIpList_whenListIsNotEmpty() throws Exception {
            Mockito.when(suspiciousIpAdminService.getSuspiciousIpList())
                    .thenReturn(List.of(new IPResponseDto(1L, "192.168.1.1"),
                            new IPResponseDto(2L, "192.168.1.2")));

            mockMvc.perform(get("/api/antifraud/suspicious-ip")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].ipAddress").exists())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Return \"Ok 200\" and empty list when black list is empty")
        void shouldReturnOkAndEmptyList_whenListIsEmpty() throws Exception {
            Mockito.when(suspiciousIpAdminService.getSuspiciousIpList())
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/antifraud/suspicious-ip")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            Mockito.verify(suspiciousIpAdminService).getSuspiciousIpList();
        }
    }
}