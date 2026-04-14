package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.CustomAccessDeniedHandler;
import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import com.tanhung.antifraudsystem.config.SecurityConfig;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.RegistrationException;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.AuthService;
import com.tanhung.antifraudsystem.service.MyUserDetailsService;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("POST /api/auth/register")
    class registerUserTest {
        @Test
        @DisplayName("Return \"Created\" status, id, name, username, and role when registering successfully")
        void shouldReturnCreated_whenRegisterSuccessfully() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;
            UserResponseDto expected = new UserResponseDto(1L, "hung nguyen", "hungnguyen", "ADMINISTRATOR");
            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.username").exists())
                    .andExpect(jsonPath("$.role").exists())
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.errors").doesNotExist());

            Mockito.verify(authService).register(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Bad Request\" and first name error, timestamp, status code when missing first name")
        void shouldReturnBadRequest_whenFirstNameIsMissing() throws Exception {
            String json = """
                    {
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.firstName").value(containsString("empty")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and last name error, timestamp, status code when missing last name")
        void shouldReturnBadRequest_whenLastNameIsMissing() throws Exception {
            String json = """
                    {
                        "firstName" : "Hung",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.lastName").value(containsString("empty")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and username error, timestamp, status code when missing username")
        void shouldReturnBadRequest_whenUsernameIsMissing() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "password" : "Hung1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").value(containsString("empty")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and password error, timestamp, status code when missing password")
        void shouldReturnBadRequest_whenPasswordIsMissing() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.password").value(containsString("empty")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and first name error, timestamp, status code " +
                "when first name's length is less than 2 letters")
        void shouldReturnBadRequest_whenFirstNameIsLessThanTwoLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "h",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.firstName").value(containsString("size")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and first name error, timestamp, status code " +
                "when first name's length is longer than 30 letters")
        void shouldReturnBadRequest_whenFirstNameIsOverThirtyLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "asdsdawadwadwadsadgdfgdqwdadsadawadwagdds",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.firstName").value(containsString("size")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Created\" and register successfully when first name contains space")
        void shouldReturnCreated_whenFirstNameContainSpace() throws Exception {
            String json = """
                    {
                        "firstName" : "Tan Hung",
                        "lastName" : "Nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;
            UserResponseDto expected = new UserResponseDto(1L, "Tan Hung Nguyen",
                    "hungnguyen","ADMINISTRATOR");

            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.username").exists())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.role").exists())
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Return \"Bad Request\" and first name error, timestamp, status code " +
                "when first name contains numbers")
        void shouldReturnBadRequest_whenFirstNameContainsNumbers() throws Exception {
            String json = """
                    {
                        "firstName" : "Hung14",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName")
                            .value(containsString("only letters!")))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and first name error, timestamp, status code " +
                "when first name contains special characters")
        void shouldReturnBadRequest_whenFirstNameContainsSpecialCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "Hung!@#$",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").value(containsString("only letters!")));

            Mockito.verifyNoInteractions(authService);
        }


        @Test
        @DisplayName("Return \"Bad Request\" and last name error, timestamp, status code " +
                "when last name's length is less than 2 letters")
        void shouldReturnBadRequest_whenLastNameIsLessThanTwoLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "n",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.lastName").value(containsString("size")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and last name error, timestamp, status code " +
                "when last name's length is longer than 30 letters")
        void shouldReturnBadRequest_whenLastNameIsOverThirtyLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyenasdasdasdwadwadsaddfgdfgdfgafsdf",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.lastName").value(containsString("size")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Created\" and id, username, name, role when last name contains space")
        void shouldReturnCreated_whenLastNameContainsSpace() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;

            UserResponseDto expected = new UserResponseDto(1L, "hung nguyen nguyen",
                    "hungnguyen","ADMINISTRATOR");

            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.username").exists())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.role").exists())
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.errors").doesNotExist());

        }

        @Test
        @DisplayName("Return \"Bad Request\" and last name error, timestamp, status code " +
                "when last name contains numbers")
        void shouldReturnBadRequest_whenLastNameContainsNumbers() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen123",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.lastName").value(containsString("only letters!")));

            Mockito.verifyNoInteractions(authService);

        }

        @Test
        @DisplayName("Return \"Bad Request\" and last name error, timestamp, status code " +
                "when last name contains special characters")
        void shouldReturnBadRequest_whenLastNameContainsSpecialCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "!@#$%^&*()_=",
                        "username" : "hungnguyen",
                        "password" : "Hung1403"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.lastName").value(containsString("only letters!")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and username error, timestamp, status code " +
                "when username's length is less than 5 characters")
        void shouldReturnBadRequest_whenUsernameIsLessThanFiveCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "tan",
                        "password" : "Hung1403"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").value(containsString("size")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and username error, timestamp, status code " +
                "when username's length is longer than 30 characters")
        void shouldReturnBadRequest_whenUsernameIsOverThirtyCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyenasdawdadwadafasghfsafdsgfdgfd",
                        "password" : "Hung1403"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").value(containsString("size")));

            Mockito.verifyNoInteractions(authService);

        }

        @Test
        @DisplayName("Return \"Bad Request\" and username error, timestamp, status code " +
                "when username contains special characters")
        void shouldReturnBadRequest_whenUsernameContainsSpecialCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "!@#$%^&*()-+",
                        "password" : "Hung1403"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").value(containsString("contain only")));

            Mockito.verifyNoInteractions(authService);

        }

        @Test
        @DisplayName("Return \"Created\" and id, name, username, role when username contains dot or underscore")
        void shouldReturnCreated_whenUsernameContainsDotAndUnderscore() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen_.",
                        "password" : "Hung1403"
                    }
                    """;

            UserResponseDto expected = new UserResponseDto(1L, "hung nguyen",
                    "hungnguyen_.","ADMINISTRATOR");
            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.username").exists())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.role").exists())
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Return \"Bad Request\" and username error, timestamp, status code " +
                "when username contains space")
        void shouldReturnBadRequest_whenUsernameContainsSpaces() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hung nguyen",
                        "password" : "Hung1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").exists());

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and password error, timestamp, status code " +
                "when password does not have at least 1 uppercase")
        void shouldReturnBadRequest_whenPasswordDoesNotHaveAtLeastOneUppercase() throws Exception{
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "hung1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400));
            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and password error, timestamp, status code " +
                "when password does not have at least 1 number")
        void shouldReturnBadRequest_whenPasswordDoesNotHaveAtLeastOneNumber() throws Exception{
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400));
            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and password error, timestamp, status code " +
                "when password does not have at least 1 lowercase")
        void shouldReturnBadRequest_whenPasswordDoesNotHaveAtLeastOneLowercase() throws Exception{
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "HUNG1403"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400));
            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and email error, timestamp, status code " +
                "when email format is invalid")
        void shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403",
                        "email" : "ngtanhung.com"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Created\" when password has special characters" +
                "when password contains special characters")
        void shouldReturnBadRequest_whenPasswordContainsSpecialCharacters() throws Exception{
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403!@#$%"
                    }
                    """;

            UserResponseDto expected = new UserResponseDto(1L, "hung nguyen",
                    "hungnguyen_.","ADMINISTRATOR");

            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.username").exists())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.role").exists())
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Return \"Conflict\" and details error, status code, timestamp " +
                "when email is already used")
        void ShouldReturnConflict_whenEmailIsAlreadyUsed() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403",
                        "email" : "test1@gmail.com"
                    }
                    """;

            Mockito.when(authService.register(Mockito.any()))
                    .thenThrow(new RegistrationException("Email is already been used!", HttpStatus.CONFLICT));

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.details").exists());


        }

        @Test
        @DisplayName("Return \"Bad Request\" and phone number error, status code, timestamp " +
                "when phone number contains letters")
        void shouldReturnBadRequest_whenPhoneNumberContainsLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403",
                        "phoneNumber" : "gsawefjajf"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.phoneNumber").value(containsString("Invalid")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and phone number error, status code, timestamp " +
                "when phone number is not exact 10 digits")
        void shouldReturnBadRequest_whenPhoneNumberDoesNotContainTenDigits() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403",
                        "phoneNumber" : "12314"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.errors.phoneNumber").value(containsString("size")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Created\" when phone number contains exact 10 digits")
        void shouldReturnCreated_whenPhoneNumberFormatIsCorrect() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403",
                        "phoneNumber" : "0123456789"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Return \"Conflict\" phone number error, status code, timestamp " +
                "when phone number is already used")
        void shouldReturnConflict_whenPhoneNumberIsAlreadyUsed() throws Exception {
            String json = """
                    {
                        "firstName" : "hung",
                        "lastName" : "nguyen",
                        "username" : "hungnguyen",
                        "password" : "Hung1403",
                        "phoneNumber" : "0123456789"
                    }
                    """;

            Mockito.when(authService.register(Mockito.any()))
                    .thenThrow(new RegistrationException("Phone number is already been used", HttpStatus.CONFLICT));

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.details").exists());

        }
    }

    @Nested
    @DisplayName("GET /api/auth/list")
    class getListUserTest {
        @Test
        @DisplayName("Return \"Ok\" status and list of users when requesting list")
        void shouldReturnOk_whenGetListOfUsersSuccessfullyWithUser() throws Exception {
            UserResponseDto mock = new UserResponseDto(1L, "hung", "hungnguyen", "ADMINISTRATOR");
            UserResponseDto mock1 = new UserResponseDto(1L, "hung", "hungnguyen", "MERCHANT");

            Mockito.when(authService.getAllUsers()).thenReturn(List.of(mock, mock1));

            mockMvc.perform(get("/api/auth/list")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

        }

        @Test
        @DisplayName("Return \"Ok\" status and empty list when no users")
        void shouldReturnOk_whenGetListOfUsersSuccessfullyWithNoUser() throws Exception{
            Mockito.when(authService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/auth/list")
                    .with(csrf()))
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("DELETE /api/auth/user/{username}")
    class deleteUserTest {

        @Test
        @DisplayName("Return \"Ok\" status and deletion response when deleting successfully")
        void shouldReturnOk_whenDeletingUserSuccessfully() throws Exception {

            Mockito.when(authService.deleteUser(Mockito.any()))
                    .thenReturn(new DeleteStatusResponse("hungnguyen", "Deleted successfully!"));

            mockMvc.perform(delete("/api/auth/user/hungnguyen")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("hungnguyen"))
                    .andExpect(jsonPath("$.status").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request\" and throw UsernameNotFound exception when username not found")
        void shouldReturnBadRequest_whenUsernameNotFound() throws Exception {

            Mockito.when(authService.deleteUser(Mockito.any()))
                    .thenThrow(new UsernameNotFoundException("Username not found!"));

            mockMvc.perform(delete("/api/auth/user/hungnguyen")
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
        @DisplayName("Return \"Ok\" status and user info that was changed when changing user role successfully")
        void shouldReturnOk_whenChangingRoleSuccessfully() throws Exception {

            String json = """
                    {
                        "username" : "hungnguyen",
                        "role" : "SUPPORT"
                    }
                    """;
            UserResponseDto expected = new UserResponseDto(1L,
                    "hung nguyen", "hungnguyen", "SUPPORT");

            Mockito.when(authService.changeRole(Mockito.any()))
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
        @DisplayName("Return \"Bad Request\" and error response when missing username")
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

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and error response when missing role")
        void shouldReturnBadRequest_whenMissingRole() throws Exception{
            String json = """
                    {
                        "username" : "hungnguyen"
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

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and error response when role is blank or space")
        void shouldReturnBadRequest_whenRoleIsBlank() throws Exception{
            String json = """
                    {
                        "username" : "hungnguyen",
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

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and error response when username is blank or space")
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

            Mockito.verifyNoInteractions(authService);
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/access")
    class activationUserTest{
        @Test
        @DisplayName("Return \"Ok\" and confirmation when activating user successfully")
        void shouldReturnOk_whenActivatingUserSuccessfully() throws Exception{

            String json = """
                    {
                        "username" : "hungnguyen",
                        "operation" : "unlock"
                    }
                    """;

            Mockito.when(authService.activateUser(Mockito.any()))
                    .thenReturn(new StatusResponse("User hungnguyen unlocked"));
            mockMvc.perform(put("/api/auth/access")
                    .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").exists());

            Mockito.verify(authService).activateUser(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Bad Request\" and error response when missing username")
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
            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and error response when username is blank")
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
            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and error response when missing operation")
        void shouldReturnBadRequest_whenMissingOperation() throws Exception{

            String json = """
                    {
                        "username" : "hungnguyen"
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
            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request\" and error response when operation is blank")
        void shouldReturnBadRequest_whenOperationIsBlank() throws Exception{

            String json = """
                    {
                        "username" : "hungnguyen",
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
            Mockito.verifyNoInteractions(authService);
        }
    }
}
