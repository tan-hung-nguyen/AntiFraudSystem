package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.CustomAccessDeniedHandler;
import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.config.SecurityConfig;
import com.tanhung.antifraudsystem.dto.response.IPResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.MyUserDetailsService;
import com.tanhung.antifraudsystem.service.SuspiciousIpAdminService;
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
 * every SuspiciousIpController endpoint, independent of request-body validation
 * or business logic (covered by SuspiciousIpControllerTest).
 */
@WebMvcTest(SuspiciousIpController.class)
@Import({SecurityConfig.class,
        CustomAccessDeniedHandler.class,
        CustomAuthenticationEntryPoint.class,
        GlobalExceptionHandler.class})
@AutoConfigureMockMvc
class SuspiciousIpControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SuspiciousIpAdminService suspiciousIpAdminService;
    @MockitoBean
    private MyUserDetailsService myUserDetailsService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String validIpJson = """
            {
                "ipAddress" : "192.168.1.1"
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
    @DisplayName("POST /api/antifraud/suspicious-ip")
    class addSuspiciousIPAddressSecurityTest {

        @Test
        @DisplayName("Return \"Created 201\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnCreated_whenUserHasSupportAuthority() throws Exception {
            Mockito.when(suspiciousIpAdminService.addIP(Mockito.any()))
                    .thenReturn(new IPResponseDto(1L, "192.168.1.1"));

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validIpJson))
                    .andExpect(status().isCreated());

            Mockito.verify(suspiciousIpAdminService).addIP(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Created 201\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnCreated_whenUserHasAdministratorAuthority() throws Exception {
            Mockito.when(suspiciousIpAdminService.addIP(Mockito.any()))
                    .thenReturn(new IPResponseDto(1L, "192.168.1.1"));

            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validIpJson))
                    .andExpect(status().isCreated());

            Mockito.verify(suspiciousIpAdminService).addIP(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validIpJson))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/antifraud/suspicious-ip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validIpJson))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/antifraud/suspicious-ip/{ip}")
    class deleteSuspiciousIPSecurityTest {

        @Test
        @DisplayName("Return \"Ok 200\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnOk_whenUserHasSupportAuthority() throws Exception {
            Mockito.when(suspiciousIpAdminService.deleteIP(Mockito.any()))
                    .thenReturn(new StatusResponseDto("IP 192.168.1.1 successfully removed!"));

            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(suspiciousIpAdminService).deleteIP("192.168.1.1");
        }

        @Test
        @DisplayName("Return \"Ok 200\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnOk_whenUserHasAdministratorAuthority() throws Exception {
            Mockito.when(suspiciousIpAdminService.deleteIP(Mockito.any()))
                    .thenReturn(new StatusResponseDto("IP 192.168.1.1 successfully removed!"));

            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(suspiciousIpAdminService).deleteIP("192.168.1.1");
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/antifraud/suspicious-ip/192.168.1.1")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }
    }

    @Nested
    @DisplayName("GET /api/antifraud/suspicious-ip")
    class getSuspiciousIpListSecurityTest {

        @Test
        @DisplayName("Return \"Ok 200\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnOk_whenUserHasSupportAuthority() throws Exception {
            Mockito.when(suspiciousIpAdminService.getSuspiciousIpList())
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/antifraud/suspicious-ip")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(suspiciousIpAdminService).getSuspiciousIpList();
        }

        @Test
        @DisplayName("Return \"Ok 200\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnOk_whenUserHasAdministratorAuthority() throws Exception {
            Mockito.when(suspiciousIpAdminService.getSuspiciousIpList())
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/antifraud/suspicious-ip")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(suspiciousIpAdminService).getSuspiciousIpList();
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(get("/api/antifraud/suspicious-ip")
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/antifraud/suspicious-ip")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(suspiciousIpAdminService);
        }
    }
}