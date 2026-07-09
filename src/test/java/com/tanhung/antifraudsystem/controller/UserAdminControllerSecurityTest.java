package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.CustomAccessDeniedHandler;
import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.config.SecurityConfig;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.MyUserDetailsService;
import com.tanhung.antifraudsystem.service.UserAdminService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the role-based access restrictions declared in SecurityConfig for
 * every UserAdminController endpoint, independent of request-body validation
 * or business logic (covered by UserAdminControllerTest).
 */
@WebMvcTest(UserAdminController.class)
@Import({SecurityConfig.class,
        CustomAccessDeniedHandler.class,
        CustomAuthenticationEntryPoint.class,
        GlobalExceptionHandler.class})
@AutoConfigureMockMvc
class UserAdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAdminService userAdminService;
    @MockitoBean
    private MyUserDetailsService myUserDetailsService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
    @DisplayName("GET /api/auth/list")
    class getListUserSecurityTest {

        @Test
        @DisplayName("Return \"Ok 200\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnOk_whenUserHasAdministratorAuthority() throws Exception {
            Mockito.when(userAdminService.getAllUsers()).thenReturn(java.util.List.of());

            mockMvc.perform(get("/api/auth/list").with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(userAdminService).getAllUsers();
        }

        @Test
        @DisplayName("Return \"Ok 200\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnOk_whenUserHasSupportAuthority() throws Exception {
            Mockito.when(userAdminService.getAllUsers()).thenReturn(java.util.List.of());

            mockMvc.perform(get("/api/auth/list").with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(userAdminService).getAllUsers();
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(get("/api/auth/list").with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/auth/list").with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/auth/user/{username}")
    class deleteUserSecurityTest {

        @Test
        @DisplayName("Return \"Ok 200\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnOk_whenUserHasAdministratorAuthority() throws Exception {
            Mockito.when(userAdminService.deleteUser(Mockito.any()))
                    .thenReturn(new DeleteStatusResponseDto("testusername", "Deleted successfully!"));

            mockMvc.perform(delete("/api/auth/user/testusername").with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(userAdminService).deleteUser("testusername");
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnForbidden_whenUserHasSupportAuthority() throws Exception {
            mockMvc.perform(delete("/api/auth/user/testusername").with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(delete("/api/auth/user/testusername").with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/auth/user/testusername").with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/role")
    class changeRoleSecurityTest {

        private final String json = """
                {
                    "username" : "testusername",
                    "role" : "SUPPORT"
                }
                """;

        @Test
        @DisplayName("Return \"Ok 200\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnOk_whenUserHasAdministratorAuthority() throws Exception {
            Mockito.when(userAdminService.changeRole(Mockito.any()))
                    .thenReturn(new UserResponseDto(1L, "Test FN", "testusername", "SUPPORT"));

            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            Mockito.verify(userAdminService).changeRole(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnForbidden_whenUserHasSupportAuthority() throws Exception {
            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/access")
    class changeUserAccessSecurityTest {

        private final String json = """
                {
                    "username" : "testusername",
                    "operation" : "unlock"
                }
                """;

        @Test
        @DisplayName("Return \"Ok 200\" when user has ADMINISTRATOR authority")
        @WithMockUser(authorities = "ADMINISTRATOR")
        void shouldReturnOk_whenUserHasAdministratorAuthority() throws Exception {
            Mockito.when(userAdminService.changeUserStatus(Mockito.any()))
                    .thenReturn(new StatusResponseDto("User testusername unlocked"));

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            Mockito.verify(userAdminService).changeUserStatus(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has SUPPORT authority")
        @WithMockUser(authorities = "SUPPORT")
        void shouldReturnForbidden_whenUserHasSupportAuthority() throws Exception {
            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Forbidden 403\" when user has MERCHANT authority")
        @WithMockUser(authorities = "MERCHANT")
        void shouldReturnForbidden_whenUserHasMerchantAuthority() throws Exception {
            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.statusCode").value(403))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Unauthorized 401\" when user is not authenticated")
        @WithAnonymousUser
        void shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.statusCode").value(401))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(userAdminService);
        }
    }
}