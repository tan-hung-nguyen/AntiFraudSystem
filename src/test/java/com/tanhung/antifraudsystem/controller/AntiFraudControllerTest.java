package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.IPResponse;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.service.AntiFraudService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.print.attribute.standard.Media;
import javax.swing.*;


import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AntiFraudController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AntiFraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AntiFraudService antiFraudService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("POST /transaction")
    class checkRequestTest{
        @Test
        @DisplayName("Return \"Ok 200\" status when request amount is greater than or equal 1.0000000" +
                " ip address in IPV4 format and card number is exactly 16 digits")
        void shouldReturnOk_whenRequestAmountIsGreaterThanOrEqual1 () throws Exception {
            String json = """
                    {
                         "amount": 1.0000,
                         "ipAddress": "192.168.1.1",
                         "cardNumber": "4000008449433403"
                    }
                    """;

            ActionResponse expected = new ActionResponse("ALLOWED", "none");
            Mockito.when(antiFraudService.checkRequest(Mockito.any()))
                    .thenReturn(expected);

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.info").exists());

            Mockito.verify(antiFraudService).checkRequest(Mockito.any());
        }


        @Test
        @DisplayName("Return \"Bad Request 400\" when amount is less than 1.0000")
        void shouldReturnBadRequest_whenRequestAmountLessThanOneDollar () throws Exception {
            String json = """
                    {
                        "amount" : 0.9999999,
                         "ipAddress": "192.168.1.1",
                         "cardNumber": "4000008449433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.amount")
                            .value(containsString("at least")))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when amount is missing")
        void shouldReturnBadRequest_whenRequestAmountIsMissing () throws Exception {
            String json = """
                    {
                         "ipAddress": "192.168.1.1",
                         "cardNumber": "4000008449433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.amount")
                            .value(containsString("null")))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip address is missing")
        void shouldReturnBadRequest_whenIpAddressIsMissing () throws Exception {
            String json = """
                    {
                         "amount" : 120.00,
                         "cardNumber": "4000008449433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress")
                            .value(containsString("null")))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number is missing")
        void shouldReturnBadRequest_whenCardNumberIsMissing() throws Exception{
            String json = """
                {
                     "amount" : 1210.0000,
                     "ipAddress": "192.168.1.1"
                }
                """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber")
                            .value(containsString("null")))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }


        @Test
        @DisplayName("Return \"Bad Request 400\" when amount value contains letter")
        void shouldReturnBadRequest_whenAmountContainLetters() throws Exception {
            String json = """
                    {
                        "amount" : "123abc",
                         "ipAddress": "192.168.1.1",
                         "cardNumber": "4000008449433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when parsing amount fail")
        void shouldReturnBadRequest_whenParsingAmountFail() throws Exception {
            String json = """
                    {
                        "amount" : abc,
                         "ipAddress": "192.168.1.1",
                         "cardNumber": "4000008449433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when parsing ip address fail")
        void shouldReturnBadRequest_whenParsingIpAddressFail() throws Exception {
            String json = """
                    {
                        "amount" : 1000,
                         "ipAddress": 192.168.1.1,
                         "cardNumber": "4000008449433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip address is missing dot")
        void shouldReturnBadRequest_whenIpAddressMissingDot() throws Exception {
            String json = """
                    {
                        "amount" : 1000.00,
                         "ipAddress": "19216811",
                         "cardNumber": "4000008449433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip address contains letters")
        void shouldReturnBadRequest_whenIpAddressContainsLetter() throws Exception {
            String json = """
                    {
                        "amount" : 1000.00,
                         "ipAddress": "192.1ae.w.1",
                         "cardNumber": "4000008449433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);
        }
        @Test
        @DisplayName("Return \"Bad Request 400\" when ip address is not in IPV4 format")
        void shouldReturnBadRequest_whenIpAddressIsNotIPV4Format() throws Exception{
                String json = """
                    {
                        "amount" : 1000.00,
                         "ipAddress": "192.168.356.1",
                         "cardNumber": "4000008449433403"
                    }
                    """;

                mockMvc.perform(post("/api/antifraud/transaction")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.statusCode").value(400))
                        .andExpect(jsonPath("$.errors.ipAddress").exists())
                        .andExpect(jsonPath("$.timestamp").exists());

                Mockito.verifyNoInteractions(antiFraudService);

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number is not exactly 16 digits")
        void shouldReturnBadRequest_whenCardNumberNotExactly16Digits() throws Exception{
            String json = """
                    {
                        "amount" : 1000.00,
                         "ipAddress": "192.168.1.1",
                         "cardNumber": "4000008449"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number contains space")
        void shouldReturnBadRequest_whenCardNumberContainSpace() throws Exception{
            String json = """
                    {
                        "amount" : 1000.00,
                         "ipAddress": "192.168.1.1",
                         "cardNumber": "4000 0084 4943 3403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number contains letters")
        void shouldReturnBadRequest_whenCardNumberContainsLetters() throws Exception{
            String json = """
                    {
                        "amount" : 1000.00,
                         "ipAddress": "192.168.1.1",
                         "cardNumber": "4000abcd4943edfc"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(antiFraudService);

        }
    }

    @Nested
    @DisplayName("POST /suspicious-ip")
    class addSuspiciousIpAddressTest{
        @Test
        @DisplayName("Return \"Created 201\" when ip address in IPV4 format")
        void shouldReturnCreated_whenIpAddressIntCorrectFormat() throws Exception{
            String json = """
                    {
                        "ipAddress" : "192.168.1.1"
                    }
                    """;

            IPResponse expected = new IPResponse(1L, "192.168.1.1");
            Mockito.when(antiFraudService.addIP(Mockito.any())).thenReturn(expected);

            mockMvc.perform(post("/api/antifraud/suspicous-ip")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.ipAddress").exists());

            Mockito.verify(antiFraudService).addIP(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip address is missing dot")
        void shouldReturnBadRequest_whenIpAddressIsMissingDot() throws Exception{
            String json = """
                    {
                        "ipAddress" : "19216811"
                    }
                    """;
            mockMvc.perform(post("/api/antifraud/suspicous-ip")
                    .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip address contains letters")
        void shouldReturnBadRequest_whenIpAddressContainsLetters() throws Exception{
            String json = """
                    {
                        "ipAddress" : "192.abc.c.1"
                    }
                    """;
            mockMvc.perform(post("/api/antifraud/suspicous-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip address format is not IPV4")
        void shouldReturnBadRequest_whenIpAddressFormatIsNotIPV4() throws Exception{
            String json = """
                    {
                        "ipAddress" : "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
                    }
                    """;
            mockMvc.perform(post("/api/antifraud/suspicous-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ip address is not provided")
        void shouldReturnBadRequest_whenIpAddressIsNull() throws Exception{
            String json = """
                    {
                        "ipAddress" : " "
                    }
                    """;
            mockMvc.perform(post("/api/antifraud/suspicous-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

        }
    }

}