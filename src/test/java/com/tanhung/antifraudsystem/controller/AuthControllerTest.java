package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.dto.response.AuthenticationResponseDto;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.AuthService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.containsString;
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
            UserRegistrationResponseDto expected = UserRegistrationResponseDto.builder()
                    .id(1L).name("TestFN TestLN").username("testusername")
                    .role("ADMINISTRATOR").jwtToken("jwtToken").build();
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
                    .andExpect(jsonPath("$.jwtToken").exists())
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
            UserRegistrationResponseDto expected = UserRegistrationResponseDto.builder()
                    .id(1L).name("TestFN TestFN TestLN").username("testusername")
                    .role("ADMINISTRATOR").jwtToken("jwtToken").build();
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
                    .andExpect(jsonPath("$.jwtToken").exists())
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

            UserRegistrationResponseDto expected = UserRegistrationResponseDto.builder()
                    .id(1L).name("TestFN TestLN TestLN").username("testusername")
                    .role("ADMINISTRATOR").jwtToken("jwtToken").build();
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
                    .andExpect(jsonPath("$.jwtToken").exists())
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

            UserRegistrationResponseDto expected = UserRegistrationResponseDto.builder()
                    .id(1L).name("TestFN TestLN").username("testusername_.")
                    .role("ADMINISTRATOR").jwtToken("jwtToken").build();
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
                    .andExpect(jsonPath("$.jwtToken").exists())
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

            UserRegistrationResponseDto expected = UserRegistrationResponseDto.builder()
                    .id(1L).name("TestFN TestLN").username("testusername")
                    .role("ADMINISTRATOR").jwtToken("jwtToken").build();

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
                    .andExpect(jsonPath("$.jwtToken").exists())
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
                    .thenThrow(new UsernameConflictException("Email is already been used!"));

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
                    .thenThrow(new UsernameConflictException("Phone number is already been used"));

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
                    .thenThrow(new RegisterNullException("Object must not be null!"));

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
                    .thenThrow(new UsernameReservedWordException("Username cannot start with reserved word!"));

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
                    .thenThrow(new UsernameConflictException("Username is already taken!"));
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
                    .thenThrow(new UsernameConflictException("Email is already in used!"));
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
                    .thenThrow(new UsernameConflictException("Phone number is already in used!"));
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
    @DisplayName("POST /api/auth/authenticate")
    class authenticateUserTest {

        @Test
        @DisplayName("Return \"OK 200\" and jwt token when username and password are valid")
        void shouldReturnOkWithJwtToken_whenCredentialsAreValid() throws Exception {
            String json = """
                    {
                        "username" : "testusername",
                        "password" : "Test1234"
                    }
                    """;

            AuthenticationResponseDto expected = AuthenticationResponseDto.builder()
                    .jwtToken("jwtToken").build();
            Mockito.when(authService.authenticate(Mockito.any())).thenReturn(expected);

            mockMvc.perform(post("/api/auth/authenticate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jwtToken").value("jwtToken"))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            Mockito.verify(authService).authenticate(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and username error, timestamp, status code when username is blank")
        void shouldReturnBadRequest_whenUsernameIsBlank() throws Exception {
            String json = """
                    {
                        "username" : "",
                        "password" : "Test1234"
                    }
                    """;

            mockMvc.perform(post("/api/auth/authenticate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").value(containsString("blank")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and username error, timestamp, status code when username is null")
        void shouldReturnBadRequest_whenUsernameIsNull() throws Exception {
            String json = """
                    {
                        "username" : null,
                        "password" : "Test1234"
                    }
                    """;

            mockMvc.perform(post("/api/auth/authenticate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").value(containsString("blank")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and username error, timestamp, status code when username is missing")
        void shouldReturnBadRequest_whenUsernameIsMissing() throws Exception {
            String json = """
                    {
                        "password" : "Test1234"
                    }
                    """;

            mockMvc.perform(post("/api/auth/authenticate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").value(containsString("blank")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and password error, timestamp, status code when password is blank")
        void shouldReturnBadRequest_whenPasswordIsBlank() throws Exception {
            String json = """
                    {
                        "username" : "testusername",
                        "password" : ""
                    }
                    """;

            mockMvc.perform(post("/api/auth/authenticate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.password").value(containsString("blank")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and password error, timestamp, status code when password is null")
        void shouldReturnBadRequest_whenPasswordIsNull() throws Exception {
            String json = """
                    {
                        "username" : "testusername",
                        "password" : null
                    }
                    """;

            mockMvc.perform(post("/api/auth/authenticate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.password").value(containsString("blank")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and password error, timestamp, status code when password is missing")
        void shouldReturnBadRequest_whenPasswordIsMissing() throws Exception {
            String json = """
                    {
                        "username" : "testusername"
                    }
                    """;

            mockMvc.perform(post("/api/auth/authenticate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.password").value(containsString("blank")));

            Mockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and both username and password errors when both are blank")
        void shouldReturnBadRequest_whenUsernameAndPasswordAreBothBlank() throws Exception {
            String json = """
                    {
                        "username" : "",
                        "password" : ""
                    }
                    """;

            mockMvc.perform(post("/api/auth/authenticate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors.username").value(containsString("blank")))
                    .andExpect(jsonPath("$.errors.password").value(containsString("blank")));

            Mockito.verifyNoInteractions(authService);
        }
    }

}
