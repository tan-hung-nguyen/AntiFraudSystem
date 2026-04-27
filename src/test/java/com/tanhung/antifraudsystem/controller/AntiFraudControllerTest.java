package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.IPResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponse;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    class addSuspiciousIpAddressTest {
        @Test
        @DisplayName("Return \"Created 201\" when ip address in IPV4 format")
        void shouldReturnCreated_whenIpAddressIntCorrectFormat() throws Exception {
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
        void shouldReturnBadRequest_whenIpAddressIsMissingDot() throws Exception {
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
        void shouldReturnBadRequest_whenIpAddressContainsLetters() throws Exception {
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
        void shouldReturnBadRequest_whenIpAddressFormatIsNotIPV4() throws Exception {
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
        void shouldReturnBadRequest_whenIpAddressIsNull() throws Exception {
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

        @Test
        @DisplayName("Return \"Bad Request 400\" when Ip address is already in the black list")
        void shouldReturnBadRequest_whenIpAddressIsAlreadyInBlackList() throws Exception{
            String json = """
                    {
                        "ipAddress" : "192.168.1.1"
                    }
                    """;
            Mockito.when(antiFraudService.addIP(Mockito.any()))
                    .thenThrow(new IPAddressConflictException("192.168.1.1 is existed in the list", HttpStatus.CONFLICT));

            mockMvc.perform(post("/api/antifraud/suspicous-ip")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.error").value("Conflict"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/antifraud/suspicious-ip/{ip}")
    class deleteSuspiciousIpAddressTest {
        @Test
        @DisplayName("Return \"Ok 200\" when removing an ip from black list successfully")
        void shouldReturnOk_whenRemovingIpAddressFromBlackListSuccessfully() throws Exception {
            Mockito.when(antiFraudService.deleteIP(Mockito.any()))
                    .thenReturn(new StatusResponse("Ip 192.168.1.1 successfully removed!"));

            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").exists());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and AntiFraud service throws IPAddressException when ip not found")
        void shouldReturnBadRequest_whenIpAddressNotFound() throws Exception {
            Mockito.when(antiFraudService.deleteIP(Mockito.any()))
                    .thenThrow(new IPAddressNotFoundException("IP address not found!", HttpStatus.NOT_FOUND));

            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.details").exists());


        }
    }
        @Nested
        @DisplayName("GET /api/antifraud/suspicious-ip")
        class getAllSuspiciousIpAddressTest{
            @Test
            @DisplayName("Return \"Ok 200\" and list of suspicious-ip when black list is not empty")
            void shouldReturnOkAndIpList_whenSuspiciousIpExist() throws Exception{
                List<IPResponse> expected = new ArrayList<>(List.of(new IPResponse(1L, "testIp"),
                                                                    new IPResponse(2L, "testIp2")));
                Mockito.when(antiFraudService.getAllSuspiciousIp()).thenReturn(expected);
                mockMvc.perform(get("/api/antifraud/suspicious-ip")
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").exists())
                        .andExpect(jsonPath("$[0].ipAddress").exists())
                        .andExpect(jsonPath("$", hasSize(2)));
            }

            @Test
            @DisplayName("Return \"Ok 200\" and empty list of suspicious-ip when black list is empty")
            void shouldReturnOkAndEmptyList_whenNoSuspiciousIpAddresses() throws Exception{
                Mockito.when(antiFraudService.getAllSuspiciousIp()).thenReturn(List.of());

                mockMvc.perform(get("/api/antifraud/suspicious-ip")
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$",hasSize(0)));

            }
        }

        @Nested
        @DisplayName("POST /api/antifraud/stolencard")
        class addStolenCardTest{
            @Test
            @DisplayName("Return \"Created 201\" when valid card number is provided")
            void shouldReturnCreated_whenAddingStolenCardNumberSuccessfully() throws Exception{
                String json = """
                        {
                            "cardNumber" : "4000008449433403"
                        }
                        """;
                Mockito.when(antiFraudService.addStolenCardNumber(Mockito.any()))
                                .thenReturn(new StolenCardResponse(1L, "4000008449433403"));
                mockMvc.perform(post("/api/antifraud/stolencard")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.cardNumber").exists());
            }

            @Test
            @DisplayName("Return \"Bad Request 400\" when cardNumber is null")
            void shouldReturnBadRequest_whenCardNumberIsNull() throws Exception{
                String json = """
                        {
                            "cardNumber" : ""
                        }
                        """;

                mockMvc.perform(post("/api/antifraud/stolencard")
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
            @DisplayName("Return \"Bad Request 400\" when cardNumber is not exactly 16 digits")
            void shouldReturnBadRequest_whenCardNumberIsNotExactly16Digits() throws Exception{
                String json = """
                        {
                            "cardNumber" : "123412412"
                        }
                        """;

                mockMvc.perform(post("/api/antifraud/stolencard")
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
            @DisplayName("Return \"Bad Request 400\" and Antifraud service throws StolenCardException " +
                    "when card number is not a valid numbers")
            void shouldReturnBadRequest_whenCardNumberIsNotValid() throws Exception{
                String json = """
                        {
                            "cardNumber" : "4000008449433401"
                        }
                        """;
                Mockito.when(antiFraudService.addStolenCardNumber(Mockito.any()))
                        .thenThrow(new InvalidCardNumberException("Card number not valid!", HttpStatus.BAD_REQUEST));
                mockMvc.perform(post("/api/antifraud/stolencard")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.statusCode").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.details").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }

            @Test
            @DisplayName("Return \"Conflict 409\" when card number is existed in stolen list")
            void shouldReturnConflict_whenCardNumberIsExistedInStolenList() throws Exception{
                String json = """
                        {
                            "cardNumber" : "4000008449433403"
                        }
                        """;
                Mockito.when(antiFraudService.addStolenCardNumber(Mockito.any()))
                        .thenThrow(new StolenCardConflictException("4000008449433403 is existed in the list",
                                                    HttpStatus.CONFLICT));
                mockMvc.perform(post("/api/antifraud/stolencard")
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
        @DisplayName("DELETE /api/antifraud/stolencard/{number}")
        class deleteStolenCardTest{
            @Test
            @DisplayName("Return \"Ok 200\" when delete stolen card from the list successfully")
            void shouldReturnOk_whenDeleteStolenCardNumberSuccessfully() throws Exception{
                Mockito.when(antiFraudService.deleteStolenCardNumber(Mockito.any()))
                        .thenReturn(new StatusResponse("Card 4000008449433403 deleted successfully!"));

                mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").exists());
            }

            @Test
            @DisplayName("Return \"Bad Request 400\" when card number is not exactly 16 digits")
            void shouldReturnBadRequest_whenCardNumberIsNotExactly16Digits() throws Exception{
                mockMvc.perform(delete("/api/antifraud/stolencard/123451")
                        .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.statusCode").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.details").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }

            @Test
            @DisplayName("Return \"Bad Request 400\" when card number contains space")
            void shouldReturnBadRequest_whenCardNumberContainsSpace() throws Exception{
                mockMvc.perform(delete("/api/antifraud/stolencard/4000+0084+4943+3403")
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.statusCode").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.details").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }

            @Test
            @DisplayName("Return \"Bad Request 400\" when card number contains letters")
            void shouldReturnBadRequest_whenCardNumberContainsLetters() throws Exception{
                mockMvc.perform(delete("/api/antifraud/stolencard/4000abc449433403")
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.statusCode").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.details").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }

            @Test
            @DisplayName("Return \"Bad Request 400\" and AntiFraudService throw StolenCardNullException when " +
                    "card number is null")
            void shouldReturnBadRequest_whenCardNumberIsNull() throws Exception{

                Mockito.when(antiFraudService.deleteStolenCardNumber(Mockito.any()))
                                .thenThrow(new StolenCardNullException("Test", HttpStatus.BAD_REQUEST));
                mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.statusCode").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.details").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }

            @Test
            @DisplayName("Return \"Not Found 404\" and AntiFraudService throw StolenCardNumberNotFoundException when " +
                    "card number not found")
            void shouldReturnBadRequest_whenCardNumberNotFound() throws Exception{

                Mockito.when(antiFraudService.deleteStolenCardNumber(Mockito.any()))
                        .thenThrow(new StolenCardNumberNotFoundException("Test", HttpStatus.NOT_FOUND));
                mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.statusCode").value(404))
                        .andExpect(jsonPath("$.error").value("Not Found"))
                        .andExpect(jsonPath("$.details").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Nested
        @DisplayName("GET /api/antifraud/stolencard")
        class getAllStolenCardTest{
            @Test
            @DisplayName("Return \"Ok 200\" and list of stolen card numbers when list is not empty")
            void shouldReturnOkAndCardNumberList_whenListIsNotEmpty() throws Exception{
                Mockito.when(antiFraudService.getAllStolenCards())
                        .thenReturn(List.of(new StolenCardResponse(1L, "number"),
                                new StolenCardResponse(2L, "number2")));

                mockMvc.perform(get("/api/antifraud/stolencard")
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").exists())
                        .andExpect(jsonPath("$[0].cardNumber").exists())
                        .andExpect(jsonPath("$",hasSize(2)));
            }

            @Test
            @DisplayName("Return \"Ok 200\" and empty list when list is empty")
            void shouldReturnOkAndEmptyList_whenListIsEmpty() throws Exception{
                Mockito.when(antiFraudService.getAllStolenCards())
                        .thenReturn(List.of());

                mockMvc.perform(get("/api/antifraud/stolencard")
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$",hasSize(0)));
            }
        }

}