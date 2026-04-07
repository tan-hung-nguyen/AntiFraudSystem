package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import com.tanhung.antifraudsystem.config.SecurityConfig;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.AntiFraudService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.swing.*;
import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AntiFraudController.class)
@Import(GlobalExceptionHandler.class)
class AntiFraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AntiFraudService antiFraudService;

    @Test
    @WithMockUser
    void shouldReturnAllowed_whenRequestAtLeastOneDollarAndLessThanOrEqualTwoHundredDollars() throws Exception{
        String json = """
                {
                    "amount" : 200.00
                }
                """;

        ActionResponse expected = new ActionResponse("ALLOWED");
        BigDecimal amount = new BigDecimal("200.00");
        Mockito.when(antiFraudService.checkAmount(amount))
                .thenReturn(expected);

        mockMvc.perform(post("/api/antifraud/transaction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ALLOWED"));

        Mockito.verify(antiFraudService).checkAmount(amount);
    }

    @Test
    @WithMockUser
    void shouldReturnManualProcessing_whenRequestOverTwoHundredAndLessThanOrEqualFifteenthHundred() throws Exception{
        String json = """
                {
                    "amount" : 1000.00
                }
                """;
        BigDecimal amount = new BigDecimal("1000.00");
        ActionResponse expected = new ActionResponse("MANUAL_PROCESSING");
        Mockito.when(antiFraudService.checkAmount(amount))
                .thenReturn(expected);

        mockMvc.perform(post("/api/antifraud/transaction")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("MANUAL_PROCESSING"));

        Mockito.verify(antiFraudService).checkAmount(amount);
    }

    @Test
    @WithMockUser
    void shouldReturnProhibited_whenRequestOverFifteenthHundredDollars() throws Exception{
        String json = """
                {
                    "amount" : 1700.00
                }
                """;
        BigDecimal amount = new BigDecimal("1700.00");
        ActionResponse expected = new ActionResponse("PROHIBITED");
        Mockito.when(antiFraudService.checkAmount(amount))
                .thenReturn(expected);

        mockMvc.perform(post("/api/antifraud/transaction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("PROHIBITED"));

        Mockito.verify(antiFraudService).checkAmount(amount);
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenRequestAmountLessThanOneDollar() throws Exception{
        String json = """
                {
                    "amount" : -100.00
                }
                """;

        mockMvc.perform(post("/api/antifraud/transaction")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount")
                        .value("Your transaction must be at least 1 dollar!"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verifyNoInteractions(antiFraudService);
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenRequestAmountIsMissing() throws Exception{
        String json = """
                {
                }
                """;

        mockMvc.perform(post("/api/antifraud/transaction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount")
                        .value("Amount can't be null!"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verifyNoInteractions(antiFraudService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturnUnauthorized_whenUserIsAnonymous() throws Exception{
        String json = """
                {
                    "amount" : 100.00
                }
                """;

        mockMvc.perform(post("/api/antifraud/transaction")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());

        Mockito.verifyNoInteractions(antiFraudService);
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenInvalidFormatDetected() throws Exception{
        String json = """
                {
                    "amount" : "asda123"
                }
                """;

        mockMvc.perform(post("/api/antifraud/transaction")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details").value(containsString("Invalid")))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verifyNoInteractions(antiFraudService);
    }
}