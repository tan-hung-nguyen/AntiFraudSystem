package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.dto.response.ActionResponseDto;
import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.exception.InvalidCardNumberException;
import com.tanhung.antifraudsystem.exception.InvalidRegionException;
import com.tanhung.antifraudsystem.exception.InvalidTransactionDateFormatException;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.mapper.TransactionMapper;
import com.tanhung.antifraudsystem.service.TransactionManagerService;
import com.tanhung.antifraudsystem.service.TransactionReviewService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionReviewService transactionReviewService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("POST /api/antifraud/transaction")
    class requestTransactionTest {

        @Test
        @DisplayName("Return \"Ok 200\" and action result when all fields are valid")
        void shouldReturnOk_whenAllFieldsAreValid() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            Mockito.when(transactionReviewService.reviewRequest(Mockito.any()))
                    .thenReturn(new ActionResponseDto(TransactionResult.ALLOWED, "none"));

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("ALLOWED"))
                    .andExpect(jsonPath("$.info").value("none"));

            Mockito.verify(transactionReviewService).reviewRequest(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Ok 200\" when amount is exactly the minimum allowed value")
        void shouldReturnOk_whenAmountIsExactlyMinimum() throws Exception {
            String json = """
                    {
                        "amount" : 1.00,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            Mockito.when(transactionReviewService.reviewRequest(Mockito.any()))
                    .thenReturn(new ActionResponseDto(TransactionResult.ALLOWED, "none"));

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("ALLOWED"))
                    .andExpect(jsonPath("$.info").value("none"));
        }

        @Test
        @DisplayName("Return \"Ok 200\" with PROHIBITED result when service flags the transaction")
        void shouldReturnOk_whenTransactionIsProhibited() throws Exception {
            String json = """
                    {
                        "amount" : 999999,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            Mockito.when(transactionReviewService.reviewRequest(Mockito.any()))
                    .thenReturn(new ActionResponseDto(TransactionResult.PROHIBITED, "amount, card-number"));

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("PROHIBITED"))
                    .andExpect(jsonPath("$.info").value("amount, card-number"));
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when amount is missing")
        void shouldReturnBadRequest_whenAmountIsMissing() throws Exception {
            String json = """
                    {
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.amount").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when amount is less than the minimum of 1.00")
        void shouldReturnBadRequest_whenAmountIsLessThanMinimum() throws Exception {
            String json = """
                    {
                        "amount" : 0.99,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.amount").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when amount is negative")
        void shouldReturnBadRequest_whenAmountIsNegative() throws Exception {
            String json = """
                    {
                        "amount" : -100,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.amount").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when amount is not a number")
        void shouldReturnBadRequest_whenAmountIsNotANumber() throws Exception {
            String json = """
                    {
                        "amount" : "abc",
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress is missing")
        void shouldReturnBadRequest_whenIpAddressIsMissing() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress is not IPV4 format")
        void shouldReturnBadRequest_whenIpAddressIsNotIpv4() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.356.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when ipAddress contains letters")
        void shouldReturnBadRequest_whenIpAddressContainsLetters() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.1ae.w.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber is missing")
        void shouldReturnBadRequest_whenCardNumberIsMissing() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber is not exactly 16 digits")
        void shouldReturnBadRequest_whenCardNumberNotExactly16Digits() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber contains spaces")
        void shouldReturnBadRequest_whenCardNumberContainsSpaces() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000 0084 4943 3403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when region is missing")
        void shouldReturnBadRequest_whenRegionIsMissing() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.region").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when region is blank")
        void shouldReturnBadRequest_whenRegionIsBlank() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : " ",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.region").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when date is missing")
        void shouldReturnBadRequest_whenDateIsMissing() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.date").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when date does not match the required format")
        void shouldReturnBadRequest_whenDateFormatIsInvalid() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "22-01-2022 16:04:00"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.date").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andDo(print());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" when date is wrong")
        void shouldReturnBadRequest_whenDateIsWrong() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-13-20T16:04:00"
                    }
                    """;
            Mockito.when(transactionReviewService.reviewRequest(Mockito.any()))
                    .thenThrow(new InvalidTransactionDateFormatException("Invalid date 2022-13-20T16:04:00"));
            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" with multiple field errors when several fields are invalid")
        void shouldReturnBadRequest_whenMultipleFieldsAreInvalid() throws Exception {
            String json = """
                    {
                        "amount" : 0,
                        "ipAddress" : "not-an-ip",
                        "cardNumber" : "123",
                        "region" : "",
                        "date" : "bad-date"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.amount").exists())
                    .andExpect(jsonPath("$.errors.ipAddress").exists())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.errors.region").exists())
                    .andExpect(jsonPath("$.errors.date").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request body is empty")
        void shouldReturnBadRequest_whenRequestBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request body is malformed JSON")
        void shouldReturnBadRequest_whenRequestBodyIsMalformedJson() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1"
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request contains an unknown field")
        void shouldReturnBadRequest_whenRequestContainsUnknownField() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00",
                        "extraField" : "unexpected"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and details when service throws InvalidCardNumberException")
        void shouldReturnBadRequest_whenServiceThrowsInvalidCardNumberException() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "EAP",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            Mockito.when(transactionReviewService.reviewRequest(Mockito.any()))
                    .thenThrow(new InvalidCardNumberException("Card number is invalid!"));

            mockMvc.perform(post("/api/antifraud/transaction")
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
        @DisplayName("Return \"Bad Request 400\" and details when service throws InvalidRegionException")
        void shouldReturnBadRequest_whenServiceThrowsInvalidRegionException() throws Exception {
            String json = """
                    {
                        "amount" : 150,
                        "ipAddress" : "192.168.1.1",
                        "cardNumber" : "4000008449433403",
                        "region" : "XXX",
                        "date" : "2022-01-22T16:04:00"
                    }
                    """;

            Mockito.when(transactionReviewService.reviewRequest(Mockito.any()))
                    .thenThrow(new InvalidRegionException("XXX is invalid!"));

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}
