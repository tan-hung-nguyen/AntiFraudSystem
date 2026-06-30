package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.CustomAccessDeniedHandler;
import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.config.SecurityConfig;
import com.tanhung.antifraudsystem.dto.request.TransactionRequest;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.AntiFraudService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AntiFraudController.class)
@Import(GlobalExceptionHandler.class)
public class AntiFraudControllerSecurityLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AntiFraudService antiFraudService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("POST /transaction")
        class requestTransactionSecurityTest {

            @Test
            @DisplayName("Should return \"Ok 200\" when user has MERCHANT authority")
            @WithMockUser(authorities = "ADMIN")
            void shouldReturnOk200_whenUserIsAuthenticatedAndHasMerchantRole() throws Exception {
                String json = """
                            {
                                "amount": 1.0000,
                                "ipAddress": "192.168.1.1",
                                "cardNumber": "4000008449433403"
                            }
                        """;

                Mockito.when(antiFraudService.checkRequest(Mockito.any()))
                        .thenReturn(new ActionResponse("ALLOWED", "none"));

                mockMvc.perform(post("/api/antifraud/transaction")
                                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.result").exists())
                        .andExpect(jsonPath("$.info").exists());
                Mockito.verify(antiFraudService).checkRequest(Mockito.any());
            }
        }

}
