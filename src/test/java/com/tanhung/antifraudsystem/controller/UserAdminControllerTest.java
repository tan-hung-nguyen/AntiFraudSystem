package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.UserAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserAdminService userAdminService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("GET /api/auth/list")
    class getListUserTest {
        @Test
        @DisplayName("Return \"Ok 200\" status and list of users when requesting list")
        void shouldReturnOk_whenGetListOfUsersSuccessfullyWithUser() throws Exception {
            UserResponseDto mock = new UserResponseDto(1L, "test1", "test1", "ADMINISTRATOR");
            UserResponseDto mock1 = new UserResponseDto(1L, "test2", "test2", "MERCHANT");

            Mockito.when(userAdminService.getAllUsers()).thenReturn(List.of(mock, mock1));

            mockMvc.perform(get("/api/auth/list")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

        }

        @Test
        @DisplayName("Return \"Ok 200\" status and empty list when no users")
        void shouldReturnOk_whenGetListOfUsersSuccessfullyWithNoUser() throws Exception{
            Mockito.when(userAdminService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/auth/list")
                            .with(csrf()))
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("DELETE /api/auth/user/{username}")
    class deleteUserTest {

        @Test
        @DisplayName("Return \"Ok 200\" status and deletion response when deleting successfully")
        void shouldReturnOk_whenDeletingUserSuccessfully() throws Exception {

            Mockito.when(userAdminService.deleteUser(Mockito.any()))
                    .thenReturn(new DeleteStatusResponseDto("test_.", "Deleted successfully!"));

            mockMvc.perform(delete("/api/auth/user/test_.")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("test_."))
                    .andExpect(jsonPath("$.status").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and throw UsernameNotFound exception when username not found")
        void shouldReturnBadRequest_whenUsernameNotFound() throws Exception {

            Mockito.when(userAdminService.deleteUser(Mockito.any()))
                    .thenThrow(new UsernameNotFoundException("Username not found!"));

            mockMvc.perform(delete("/api/auth/user/testusername")
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when username is blank")
        void shouldReturnBadRequest_whenUsernameIsBlank() throws Exception {
            mockMvc.perform(delete("/api/auth/user/     ")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/role")
    class changeRoleTest {
        @Test
        @DisplayName("Return \"Ok 200\" status and user info that was changed when changing user role successfully")
        void shouldReturnOk_whenChangingRoleSuccessfully() throws Exception {

            String json = """
                    {
                        "username" : "testusername",
                        "role" : "SUPPORT"
                    }
                    """;
            UserResponseDto expected = new UserResponseDto(1L,
                    "TestFN TestLN", "testusername", "SUPPORT");

            Mockito.when(userAdminService.changeRole(Mockito.any()))
                    .thenReturn(expected);
            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.username").exists())
                    .andExpect(jsonPath("$.role").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and error response when missing username")
        void shouldReturnBadRequest_whenMissingUsername() throws Exception{
            String json = """
                    {
                        "role" : "SUPPORT"
                    }
                    """;

            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.username").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400));

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and error response when missing role")
        void shouldReturnBadRequest_whenMissingRole() throws Exception{
            String json = """
                    {
                        "username" : "testusername"
                    }
                    """;

            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.role").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400));

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and error response when role is blank or space")
        void shouldReturnBadRequest_whenRoleIsBlank() throws Exception{
            String json = """
                    {
                        "username" : "tester",
                        "role" : " "
                    }
                    """;

            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.role").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400));

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and error response when username is blank or space")
        void shouldReturnBadRequest_whenUsernameIsBlank() throws Exception{
            String json = """
                    {
                        "username" : " ",
                        "role" : "SUPPORT"
                    }
                    """;

            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.username").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400));

            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when username not found!")
        void shouldReturnBadRequest_whenUsernameNotFound() throws Exception{
            String json = """
                    {
                        "username" : "test",
                        "role" : "SUPPORT"
                    }
                    """;
            Mockito.when(userAdminService.changeRole(Mockito.any()))
                    .thenThrow(new UsernameNotFoundException("Username not found!"));
            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.details").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when role change request is other than support or merchant")
        void shouldReturnBadRequest_whenRoleRequestIsOtherThanSupportOrMerchant() throws Exception{
            String json = """
                    {
                        "username" : "testusername",
                        "role" : "HELPER"
                    }
                    """;
            Mockito.when(userAdminService.changeRole(Mockito.any()))
                    .thenThrow(new RoleNotAvailableException("Only Support or Merchant role are available!"));
            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists());

        }

        @Test
        @DisplayName("Return \"Conflict 409\" when role value is already provided!")
        void shouldReturnConflict_whenRoleIsAlreadyProvided() throws Exception{
            String json = """
                    {
                        "username" : "test",
                        "role" : "SUPPORT"
                    }
                    """;
            Mockito.when(userAdminService.changeRole(Mockito.any()))
                    .thenThrow(new RoleConflictException("test has been provided this role!"));
            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.details").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when attempting change role on admin")
        void shouldReturnBadRequest_whenAttemptingChangeRoleOnAdmin() throws Exception{
            String json = """
                    {
                        "username" : "test",
                        "role" : "SUPPORT"
                    }
                    """;
            Mockito.when(userAdminService.changeRole(Mockito.any()))
                    .thenThrow(new RoleChangeException("This is admin! You cannot make change role on admin."));
            mockMvc.perform(put("/api/auth/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists());

        }

    }

    @Nested
    @DisplayName("PUT /api/auth/access")
    class activationUserTest{
        @Test
        @DisplayName("Return \"Ok 200\" and confirmation when activating user successfully")
        void shouldReturnOk_whenActivatingUserSuccessfully() throws Exception{

            String json = """
                    {
                        "username" : "testusername",
                        "operation" : "unlock"
                    }
                    """;

            Mockito.when(userAdminService.changeUserStatus(Mockito.any()))
                    .thenReturn(new StatusResponseDto("User testusername unlocked"));
            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").exists());

            Mockito.verify(userAdminService).changeUserStatus(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and error response when missing username")
        void shouldReturnBadRequest_whenMissingUsername() throws Exception{

            String json = """
                    {
                        "operation" : "unlock"
                    }
                    """;

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.username").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());
            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and error response when username is blank")
        void shouldReturnBadRequest_whenUsernameIsBlank() throws Exception{

            String json = """
                    {
                        "username" : " ",
                        "operation" : "unlock"
                    }
                    """;

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.username").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());
            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and error response when missing operation")
        void shouldReturnBadRequest_whenMissingOperation() throws Exception{

            String json = """
                    {
                        "username" : "testusername"
                    }
                    """;

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.operation").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());
            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and error response when operation is blank")
        void shouldReturnBadRequest_whenOperationIsBlank() throws Exception{

            String json = """
                    {
                        "username" : "testusername",
                        "operation" : " "
                    }
                    """;

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.operation").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());
            Mockito.verifyNoInteractions(userAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when username not found")
        void shouldReturnBadRequest_whenUsernameNotFound() throws Exception{
            String json = """
                    {
                        "username" : "testusername",
                        "operation" : "unlock"
                    }
                    """;
            Mockito.when(userAdminService.changeUserStatus(Mockito.any()))
                    .thenThrow(new UsernameNotFoundException("Username not found!"));

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when attempting deactivate admin")
        void shouldReturnBadRequest_whenDeactivatingAdmin() throws Exception{
            String json = """
                    {
                        "username" : "testusername",
                        "operation" : "lock"
                    }
                    """;
            Mockito.when(userAdminService.changeUserStatus(Mockito.any()))
                    .thenThrow(new UserStatusChangeException("You cannot deactivate admin!"));

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when attempting activate an active user")
        void shouldReturnBadRequest_whenActivatedAnActiveUser() throws Exception{
            String json = """
                    {
                        "username" : "testusername",
                        "operation" : "unlock"
                    }
                    """;
            Mockito.when(userAdminService.changeUserStatus(Mockito.any()))
                    .thenThrow(new UserStatusChangeException("User testusername has already been activated!"));

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when attempting deactivate an inactive user")
        void shouldReturnBadRequest_whenDeactivatedAnInactiveUser() throws Exception{
            String json = """
                    {
                        "username" : "testusername",
                        "operation" : "lock"
                    }
                    """;
            Mockito.when(userAdminService.changeUserStatus(Mockito.any()))
                    .thenThrow(new UserStatusChangeException("User testusername had already been deactivated!"));

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when providing invalid operation")
        void shouldReturnBadRequest_whenProvidingInvalidOperation() throws Exception{
            String json = """
                    {
                        "username" : "testusername",
                        "operation" : "set"
                    }
                    """;
            Mockito.when(userAdminService.changeUserStatus(Mockito.any()))
                    .thenThrow(new InvalidOperationChangeException("Invalid Operation!"));

            mockMvc.perform(put("/api/auth/access")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.details").exists());
        }

    }
}