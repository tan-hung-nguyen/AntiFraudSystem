package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.CustomAccessDeniedHandler;
import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.config.SecurityConfig;
import com.tanhung.antifraudsystem.dto.response.ActionResponseDto;
import com.tanhung.antifraudsystem.enums.TransactionResult;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.MyUserDetailsService;
import com.tanhung.antifraudsystem.service.TransactionReviewService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the role-based access restriction declared in SecurityConfig for
 * TransactionController, independent of request-body validation or business
 * logic (covered by TransactionControllerTest).
 */
@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class,
        CustomAccessDeniedHandler.class,
        CustomAuthenticationEntryPoint.class,
        GlobalExceptionHandler.class})
@AutoConfigureMockMvc
class TransactionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionReviewService transactionReviewService;
    @MockitoBean
    private MyUserDetailsService myUserDetailsService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String validTransactionJson = """
            {
                "amount" : 150,
                "ipAddress" : "192.168.1.1",
                "cardNumber" : "4000008449433403",
                "region" : "EAP",
                "date" : "2022-01-22T16:04:00"
            }
            """;

    @BeforeEach
    /*Disables the JWT filter so @WithMockUser alone controls the authenticated principal*/
    void setUp() throws Exception {
        Mockito.doAnswer(invocation -> {
                    HttpServletRequest request = invocation.getArgument(0);
                    HttpServletResponse response = invocation.getArgument(1);
                    FilterChain filterChain = invocation.getArgument(2);

                    filterChain.doFilter(request, response);

                    return null;
                }).when(jwtAuthenticationFilter)
                .doFilter(
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any()
                );
    }

    @Nested
    @DisplayName("POST /api/antifraud/transaction")
    class requestTransactionSecurityTest {

        @Test
        @DisplayName("Return \"Ok 200\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnOk_whenUserHasMerchantAuthority() throws Exception {
            Mockito.when(transactionReviewService.reviewRequest(Mockito.any()))
                    .thenReturn(new ActionResponseDto(TransactionResult.ALLOWED, "none"));

            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validTransactionJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("ALLOWED"))
                    .andExpect(jsonPath("$.info").value("none"));

            Mockito.verify(transactionReviewService).reviewRequest(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnForbidden_whenUserHasAdministratorAuthority() throws Exception {
            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validTransactionJson))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnForbidden_whenUserHasSupportAuthority() throws Exception {
            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validTransactionJson))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/antifraud/transaction")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validTransactionJson))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(transactionReviewService);
        }
    }
}