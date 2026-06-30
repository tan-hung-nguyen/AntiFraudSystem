package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("POST /api/auth/register")
    class registerUserTest {
        @Test
        @DisplayName("Return \"Created 201\" status, id, name, username, role, and jwt token " +
                "when providing 4 required fields (firstName, lastName, username, password)")
        void shouldReturnCreated_whenRegisterSuccessfully() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234"
                    }
                    """;
            UserResponseDto userDto = new UserResponseDto(1L, "TestFN TestLN", "testusername", "ADMINISTRATOR");
            Map<String, Object> expected = Map.of("user_info", userDto, "token", "jwtToken" );
            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user_info.id").exists())
                    .andExpect(jsonPath("$.user_info.name").exists())
                    .andExpect(jsonPath("$.user_info.username").exists())
                    .andExpect(jsonPath("$.user_info.role").exists())
                    .andExpect(jsonPath("$.user_info.password").doesNotExist())
                    .andExpect(jsonPath("$.user_info.email").doesNotExist())
                    .andExpect(jsonPath("$.user_info.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.errors").doesNotExist());

            Mockito.verify(authService).register(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and first name error, timestamp, status code when missing first name")
        void shouldReturnBadRequest_whenFirstNameIsMissing() throws Exception {
            String json = """
                    {
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234"
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
        @DisplayName("Return \"Bad Request 400\" and last name error, timestamp, status code when missing last name")
        void shouldReturnBadRequest_whenLastNameIsMissing() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "username" : "testusername",
                        "password" : "Test1234"
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
        @DisplayName("Return \"Bad Request 400\" and username error, timestamp, status code when missing username")
        void shouldReturnBadRequest_whenUsernameIsMissing() throws Exception {
            String json = """
                    {
                        "firstName" : "testFN",
                        "lastName" : "testLN",
                        "password" : "Test1234"
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
        @DisplayName("Return \"Bad Request 400\" and password error, timestamp, status code when missing password")
        void shouldReturnBadRequest_whenPasswordIsMissing() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername"
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
        @DisplayName("Return \"Bad Request 400\" and first name error, timestamp, status code " +
                "when first name's length is less than 2 letters")
        void shouldReturnBadRequest_whenFirstNameIsLessThanTwoLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "t",
                        "lastName" : "testLN",
                        "username" : "testusername",
                        "password" : "Test123"
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
        @DisplayName("Return \"Bad Request 400\" and first name error, timestamp, status code " +
                "when first name's length is longer than 30 letters")
        void shouldReturnBadRequest_whenFirstNameIsOverThirtyLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "asdsdawadwadwadsadgdfgdqwdadsadawadwagdds",
                        "lastName" : "testLN",
                        "username" : "testusername",
                        "password" : "Test1234"
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
        @DisplayName("Return \"Created 201\" and register successfully when first name contains space")
        void shouldReturnCreated_whenFirstNameContainSpace() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234"
                    }
                    """;
            UserResponseDto userDto = new UserResponseDto(1L, "TestFN TestFN TestLN",
                    "testusername","ADMINISTRATOR");
            Map<String, Object> expected = Map.of("user_info", userDto, "token", "jwtToken");
            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user_info.id").exists())
                    .andExpect(jsonPath("$.user_info.name").exists())
                    .andExpect(jsonPath("$.user_info.username").exists())
                    .andExpect(jsonPath("$.user_info.role").exists())
                    .andExpect(jsonPath("$.user_info.password").doesNotExist())
                    .andExpect(jsonPath("$.user_info.email").doesNotExist())
                    .andExpect(jsonPath("$.user_info.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and first name error, timestamp, status code " +
                "when first name contains numbers")
        void shouldReturnBadRequest_whenFirstNameContainsNumbers() throws Exception {
            String json = """
                    {
                        "firstName" : "Test12",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234"
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
        @DisplayName("Return \"Bad Request 400\" and first name error, timestamp, status code " +
                "when first name contains special characters")
        void shouldReturnBadRequest_whenFirstNameContainsSpecialCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "Test!@#$",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1203"
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
        @DisplayName("Return \"Bad Request 400\" and last name error, timestamp, status code " +
                "when last name's length is less than 2 letters")
        void shouldReturnBadRequest_whenLastNameIsLessThanTwoLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "t",
                        "username" : "testusername",
                        "password" : "Test1403"
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
        @DisplayName("Return \"Bad Request 400\" and last name error, timestamp, status code " +
                "when last name's length is longer than 30 letters")
        void shouldReturnBadRequest_whenLastNameIsOverThirtyLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "Testasdasdasdwadwadsaddfgdfgdfgafsdf",
                        "username" : "testusername",
                        "password" : "Test1234"
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
        @DisplayName("Return \"Created 201\" and id, username, name, role, and jwt token when last name contains space")
        void shouldReturnCreated_whenLastNameContainsSpace() throws Exception {
            String json = """
                    {
                        "firstName" : "testFN",
                        "lastName" : "TestLN TestLn",
                        "username" : "testusername",
                        "password" : "Test1234"
                    }
                    """;

            UserResponseDto userDto = new UserResponseDto(1L, "TestFN TestLN TestLN",
                    "testusername","ADMINISTRATOR");
            Map<String, Object> expected = Map.of("user_info", userDto, "token", "jwtToken");
            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user_info.id").exists())
                    .andExpect(jsonPath("$.user_info.name").exists())
                    .andExpect(jsonPath("$.user_info.username").exists())
                    .andExpect(jsonPath("$.user_info.role").exists())
                    .andExpect(jsonPath("$.user_info.password").doesNotExist())
                    .andExpect(jsonPath("$.user_info.email").doesNotExist())
                    .andExpect(jsonPath("$.user_info.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.errors").doesNotExist());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and last name error, timestamp, status code " +
                "when last name contains numbers")
        void shouldReturnBadRequest_whenLastNameContainsNumbers() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN12",
                        "username" : "testusername",
                        "password" : "test123"
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
        @DisplayName("Return \"Bad Request 400\" and last name error, timestamp, status code " +
                "when last name contains special characters")
        void shouldReturnBadRequest_whenLastNameContainsSpecialCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "!@#$%^&*()_=",
                        "username" : "testusername",
                        "password" : "Test1233"
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
        @DisplayName("Return \"Bad Request 400\" and username error, timestamp, status code " +
                "when username's length is less than 5 characters")
        void shouldReturnBadRequest_whenUsernameIsLessThanFiveCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "test",
                        "password" : "Test123"
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
        @DisplayName("Return \"Bad Request 400\" and username error, timestamp, status code " +
                "when username's length is longer than 30 characters")
        void shouldReturnBadRequest_whenUsernameIsOverThirtyCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusernameasdawdadwadafasghfsafdsgfdgfd",
                        "password" : "Test123"
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
        @DisplayName("Return \"Bad Request 400\" and username error, timestamp, status code " +
                "when username contains special characters")
        void shouldReturnBadRequest_whenUsernameContainsSpecialCharacters() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "!@#$%^&*()-+",
                        "password" : "Test123"
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
        @DisplayName("Return \"Created 201\" and id, name, username, role, and jwt token when username contains dot or underscore")
        void shouldReturnCreated_whenUsernameContainsDotAndUnderscore() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername_.",
                        "password" : "Test123"
                    }
                    """;

            UserResponseDto userDto = new UserResponseDto(1L, "TestFN TestLN",
                    "testusername_.","ADMINISTRATOR");
            Map<String, Object> expected = Map.of("user_info", userDto, "token", "jwtToken");
            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user_info.id").exists())
                    .andExpect(jsonPath("$.user_info.name").exists())
                    .andExpect(jsonPath("$.user_info.username").exists())
                    .andExpect(jsonPath("$.user_info.role").exists())
                    .andExpect(jsonPath("$.user_info.password").doesNotExist())
                    .andExpect(jsonPath("$.user_info.email").doesNotExist())
                    .andExpect(jsonPath("$.user_info.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and username error, timestamp, status code " +
                "when username contains space")
        void shouldReturnBadRequest_whenUsernameContainsSpaces() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "test username",
                        "password" : "Test103"
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
        @DisplayName("Return \"Bad Request 400\" and password error, timestamp, status code " +
                "when password does not have at least 1 uppercase")
        void shouldReturnBadRequest_whenPasswordDoesNotHaveAtLeastOneUppercase() throws Exception{
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "test103"
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
        @DisplayName("Return \"Bad Request 400\" and password error, timestamp, status code " +
                "when password does not have at least 1 number")
        void shouldReturnBadRequest_whenPasswordDoesNotHaveAtLeastOneNumber() throws Exception{
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test"
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
        @DisplayName("Return \"Bad Request 400\" and password error, timestamp, status code " +
                "when password does not have at least 1 lowercase")
        void shouldReturnBadRequest_whenPasswordDoesNotHaveAtLeastOneLowercase() throws Exception{
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "TEST1234"
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
        @DisplayName("Return \"Bad Request 400\" and email error, timestamp, status code " +
                "when email format is invalid")
        void shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test123",
                        "email" : "test.com"
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
        @DisplayName("Return \"Created 201\" when password contains special characters")
        void shouldReturnCreated_whenPasswordContainsSpecialCharacters() throws Exception{
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test3!@#$%"
                    }
                    """;

            UserResponseDto userDto = new UserResponseDto(1L, "TestFN TestLN",
                    "testusername","ADMINISTRATOR");

            Map<String, Object> expected = Map.of("user_info", userDto, "token", "jwtToken");
            Mockito.when(authService.register(Mockito.any())).thenReturn(expected);

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user_info.id").exists())
                    .andExpect(jsonPath("$.user_info.name").exists())
                    .andExpect(jsonPath("$.user_info.username").exists())
                    .andExpect(jsonPath("$.user_info.role").exists())
                    .andExpect(jsonPath("$.user_info.password").doesNotExist())
                    .andExpect(jsonPath("$.user_info.email").doesNotExist())
                    .andExpect(jsonPath("$.user_info.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Return \"Conflict 409\" and details error, status code, timestamp " +
                "when email is already used")
        void ShouldReturnConflict_whenEmailIsAlreadyUsed() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234",
                        "email" : "test1@gmail.com"
                    }
                    """;

            Mockito.when(authService.register(Mockito.any()))
                    .thenThrow(new UsernameConflictException("Email is already been used!", HttpStatus.CONFLICT));

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
        @DisplayName("Return \"Bad Request 400\" and phone number error, status code, timestamp " +
                "when phone number contains letters")
        void shouldReturnBadRequest_whenPhoneNumberContainsLetters() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234",
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
        @DisplayName("Return \"Bad Request 400\" and phone number error, status code, timestamp " +
                "when phone number is not exact 10 digits")
        void shouldReturnBadRequest_whenPhoneNumberDoesNotContainTenDigits() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234",
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
        @DisplayName("Return \"Created 201\" when phone number contains exact 10 digits")
        void shouldReturnCreated_whenPhoneNumberFormatIsCorrect() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234",
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
        @DisplayName("Return \"Conflict 409\" phone number error, status code, timestamp " +
                "when phone number is already used")
        void shouldReturnConflict_whenPhoneNumberIsAlreadyUsed() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234",
                        "phoneNumber" : "0123456789"
                    }
                    """;

            Mockito.when(authService.register(Mockito.any()))
                    .thenThrow(new UsernameConflictException("Phone number is already been used", HttpStatus.CONFLICT));

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
        @DisplayName("Return \"Bad Request 400\" when userRequest object is null")
        void shouldReturnBadRequest_whenUserRequestObjectIsNull() throws Exception{
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234",
                    }
                    """;
            Mockito.when(authService.register(null))
                    .thenThrow(new RegisterNullException("Object must not be null!", HttpStatus.BAD_REQUEST));

            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.details").exists());

        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when username starts with \"admin\" or \"root\"")
        void shouldReturnBadRequest_whenUsernameStartsWithAdminOrRoot() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "adminusername",
                        "password" : "Test1412",
                        "phoneNumber" : "0123456789"
                    }
                    """;
            Mockito.when(authService.register(Mockito.any()))
                    .thenThrow(new UsernameReservedWordException("Username cannot start with reserved word!",
                                                            HttpStatus.BAD_REQUEST));

            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.details").exists());

        }

        @Test
        @DisplayName("Return \"Conflict 409\" when username is taken")
        void shouldReturnConflict_whenUsernameIsTaken() throws Exception{
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234",
                        "phoneNumber" : "0123456789"
                    }
                    """;
            Mockito.when(authService.register(Mockito.any()))
                    .thenThrow(new UsernameConflictException("Username is already taken!", HttpStatus.CONFLICT));
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.details").exists());

        }

        @Test
        @DisplayName("Return \"Conflict 409\" when email is already in used")
        void shouldReturnConflict_whenEmailIsAlreadyUsed() throws Exception{
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1203",
                        "email" : "test@gmail.com"
                    }
                    """;
            Mockito.when(authService.register(Mockito.any()))
                    .thenThrow(new UsernameConflictException("Email is already in used!", HttpStatus.CONFLICT));
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.details").exists());

        }

        @Test
        @DisplayName("Return \"Conflict 409\" when phone number is already in used")
        void shouldReturnConflict_whenPhoneNumberIsAlreadyInUsed() throws Exception{
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "testusername",
                        "password" : "Test1234",
                        "phoneNumber" : "0123456789"
                    }
                    """;
            Mockito.when(authService.register(Mockito.any()))
                    .thenThrow(new UsernameConflictException("Phone number is already in used!", HttpStatus.CONFLICT));
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.details").exists());

        }
        @Test
        @DisplayName("Return \"Bad Request 400\" when unknown field provided")
        void shouldReturnBadRequest_whenUnknownFieldProvided() throws Exception {
            String json = """
                    {
                        "firstName" : "TestFN",
                        "lastName" : "TestLN",
                        "username" : "adminusername",
                        "password" : "Test1412",
                        "unknown" : "testUnknown"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.details").exists());

        }

    }

    @Nested
    @DisplayName("GET /api/auth/list")
    class getListUserTest {
        @Test
        @DisplayName("Return \"Ok 200\" status and list of users when requesting list")
        void shouldReturnOk_whenGetListOfUsersSuccessfullyWithUser() throws Exception {
            UserResponseDto mock = new UserResponseDto(1L, "test1", "test1", "ADMINISTRATOR");
            UserResponseDto mock1 = new UserResponseDto(1L, "test2", "test2", "MERCHANT");

            Mockito.when(authService.getAllUsers()).thenReturn(List.of(mock, mock1));

            mockMvc.perform(get("/api/auth/list")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

        }

        @Test
        @DisplayName("Return \"Ok 200\" status and empty list when no users")
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
        @DisplayName("Return \"Ok 200\" status and deletion response when deleting successfully")
        void shouldReturnOk_whenDeletingUserSuccessfully() throws Exception {

            Mockito.when(authService.deleteUser(Mockito.any()))
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

            Mockito.when(authService.deleteUser(Mockito.any()))
                    .thenThrow(new UsernameNotFoundException("Username not found!"));

            mockMvc.perform(delete("/api/auth/user/testusername")
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
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

            Mockito.verifyNoInteractions(authService);
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

            Mockito.verifyNoInteractions(authService);
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

            Mockito.verifyNoInteractions(authService);
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

            Mockito.verifyNoInteractions(authService);
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
            Mockito.when(authService.changeRole(Mockito.any()))
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
            Mockito.when(authService.changeRole(Mockito.any()))
                    .thenThrow(new RoleNotAvailableException("Only Support or Merchant role are available!",
                                                        HttpStatus.BAD_REQUEST));
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
            Mockito.when(authService.changeRole(Mockito.any()))
                    .thenThrow(new RoleConflictException("test has been provided this role!",
                            HttpStatus.CONFLICT));
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
            Mockito.when(authService.changeRole(Mockito.any()))
                    .thenThrow(new RoleChangeException("This is admin! You cannot make change role on admin.",
                            HttpStatus.BAD_REQUEST));
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

            Mockito.when(authService.changeUserStatus(Mockito.any()))
                    .thenReturn(new StatusResponseDto("User hungnguyen unlocked"));
            mockMvc.perform(put("/api/auth/access")
                    .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").exists());

            Mockito.verify(authService).changeUserStatus(Mockito.any());
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
            Mockito.verifyNoInteractions(authService);
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
            Mockito.verifyNoInteractions(authService);
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
            Mockito.verifyNoInteractions(authService);
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
            Mockito.verifyNoInteractions(authService);
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
            Mockito.when(authService.changeUserStatus(Mockito.any()))
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
            Mockito.when(authService.changeUserStatus(Mockito.any()))
                    .thenThrow(new UserStatusChangeException("You cannot deactivate admin!",
                            HttpStatus.BAD_REQUEST));

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
            Mockito.when(authService.changeUserStatus(Mockito.any()))
                    .thenThrow(new UserStatusChangeException("User testusername has already been activated!",
                            HttpStatus.BAD_REQUEST));

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
            Mockito.when(authService.changeUserStatus(Mockito.any()))
                    .thenThrow(new UserStatusChangeException("User testusername had already been deactivated!",
                            HttpStatus.BAD_REQUEST));

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
            Mockito.when(authService.changeUserStatus(Mockito.any()))
                    .thenThrow(new InvalidOperationChangeException("Invalid Operation!",
                            HttpStatus.BAD_REQUEST));

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
