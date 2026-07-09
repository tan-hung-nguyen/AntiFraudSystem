package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.CustomAccessDeniedHandler;
import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.config.SecurityConfig;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponseDto;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.MyUserDetailsService;
import com.tanhung.antifraudsystem.service.StolenCardAdminService;
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

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the role-based access restrictions declared in SecurityConfig for
 * every StolenCardController endpoint, independent of request-body validation
 * or business logic (covered by StolenCardControllerTest).
 */
@WebMvcTest(StolenCardController.class)
@Import({SecurityConfig.class,
        CustomAccessDeniedHandler.class,
        CustomAuthenticationEntryPoint.class,
        GlobalExceptionHandler.class})
@AutoConfigureMockMvc
class StolenCardControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StolenCardAdminService stolenCardAdminService;
    @MockitoBean
    private MyUserDetailsService myUserDetailsService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String validCardJson = """
            {
                "cardNumber" : "4000008449433403"
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
    @DisplayName("POST /api/antifraud/stolencard")
    class addStolenCardNumberSecurityTest {

        @Test
        @DisplayName("Return \"Created 201\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnCreated_whenUserHasSupportAuthority() throws Exception {
            Mockito.when(stolenCardAdminService.addStolenCardNumber(Mockito.any()))
                    .thenReturn(new StolenCardResponseDto(1L, "4000008449433403"));

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCardJson))
                    .andExpect(status().isCreated());

            Mockito.verify(stolenCardAdminService).addStolenCardNumber(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Created 201\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnCreated_whenUserHasAdministratorAuthority() throws Exception {
            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCardJson))
                    .andExpect(status().isCreated());

            Mockito.verify(stolenCardAdminService).addStolenCardNumber(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCardJson))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCardJson))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/antifraud/stolencard/{number}")
    class deleteStolenCardNumberSecurityTest {

        @Test
        @DisplayName("Return \"Ok 200\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnOk_whenUserHasSupportAuthority() throws Exception {
            Mockito.when(stolenCardAdminService.deleteStolenCardNumber(Mockito.any()))
                    .thenReturn(new StatusResponseDto("Card 4000008449433403 successfully removed!"));

            mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(stolenCardAdminService).deleteStolenCardNumber("4000008449433403");
        }

        @Test
        @DisplayName("Return \"Ok 200\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnOk_whenUserHasAdministratorAuthority() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(stolenCardAdminService).deleteStolenCardNumber("4000008449433403");
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }
    }

    @Nested
    @DisplayName("GET /api/antifraud/stolencard")
    class getStolenCardListSecurityTest {

        @Test
        @DisplayName("Return \"Ok 200\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnOk_whenUserHasSupportAuthority() throws Exception {
            Mockito.when(stolenCardAdminService.getStolenCardList())
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/antifraud/stolencard")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(stolenCardAdminService).getStolenCardList();
        }

        @Test
        @DisplayName("Return \"Ok 200\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnOk_whenUserHasAdministratorAuthority() throws Exception {
            mockMvc.perform(get("/api/antifraud/stolencard")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(stolenCardAdminService).getStolenCardList();
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(get("/api/antifraud/stolencard")
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/antifraud/stolencard")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }
    }
}