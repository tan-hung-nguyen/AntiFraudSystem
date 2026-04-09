package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.CustomAccessDeniedHandler;
import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import com.tanhung.antifraudsystem.config.SecurityConfig;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.RegistrationException;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.AuthService;
import com.tanhung.antifraudsystem.service.MyUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void shouldReturnCreated_whenRegisterSuccessfully() throws Exception {
        String json = """
                {
                    "firstName" : "hung",
                    "lastName" : "nguyen",
                    "username" : "hungnguyen",
                    "password" : "Hung1403"
                }
                """;
        UserResponseDto expected = new UserResponseDto(1L,"hung nguyen", "hungnguyen", "ADMINISTRATOR");
        Mockito.when(authService.register(Mockito.any())).thenReturn(expected);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("hung nguyen"))
                .andExpect(jsonPath("$.username").value("hungnguyen"))
                .andExpect(jsonPath("$.role").value("ADMINISTRATOR"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.phoneNumber").doesNotExist());

        Mockito.verify(authService).register(Mockito.any());
    }

    @Test
    void shouldReturnBadRequest_whenFirstNameIsMissing() throws Exception{
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
    void shouldReturnBadRequest_whenLastNameIsMissing() throws Exception{
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
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsMissing() throws Exception{
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
    }

    @Test
    void shouldReturnBadRequest_whenPasswordIsMissing() throws Exception{
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
    }

    @Test
    void shouldReturnBadRequest_whenFirstNameIsLessThanTwoCharacters() throws Exception{
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
                .andExpect(jsonPath("$.errors.firstName").value(containsString("length")));

    }

    @Test
    void shouldReturnBadRequest_whenFirstNameIsOverThirtyCharacters() throws Exception{
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
                .andExpect(jsonPath("$.errors.firstName").value(containsString("length")));

    }
    @Test
    void shouldReturnCreated_whenFirstNameContainSpace() throws Exception{
        String json = """
                {
                    "firstName" : "Tan Hung",
                    "lastName" : "nguyen",
                    "username" : "hungnguyen",
                    "password" : "Hung1403"
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
    void shouldReturnBadRequest_whenFirstNameContainsNumbers() throws Exception{
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
                .andExpect(jsonPath("$.errors.firstName").value(containsString("only letters!")));
    }

    @Test
    void shouldReturnBadRequest_whenFirstNameContainsSpecialCharacters() throws Exception{
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
    }


    @Test
    void shouldReturnBadRequest_whenLastNameIsLessThanTwoCharacters() throws Exception{
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
                .andExpect(jsonPath("$.errors.lastName").value(containsString("length")));

    }

    @Test
    void shouldReturnBadRequest_whenLastNameIsOverThirtyCharacters() throws Exception{
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
                .andExpect(jsonPath("$.errors.lastName").value(containsString("length")));
    }

    @Test
    void shouldReturnCreated_whenLastNameContainsSpace() throws Exception{
        String json = """
                {
                    "firstName" : "hung",
                    "lastName" : "nguyen nguyen",
                    "username" : "hungnguyen",
                    "password" : "Hung1403"
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
    void shouldReturnBadRequest_whenLastNameContainsNumbers() throws Exception{
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

    }

    @Test
    void shouldReturnBadRequest_whenLastNameContainsSpecialCharacters() throws Exception{
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
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsLessThanFiveCharacters() throws Exception{
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
                .andExpect(jsonPath("$.errors.username").value(containsString("length")));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsOverThirtyCharacters() throws Exception{
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
                .andExpect(jsonPath("$.errors.username").value(containsString("length")));

    }

    @Test
    void shouldReturnBadRequest_whenUsernameContainsSpecialCharacters() throws Exception{
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

    }

    @Test
    void shouldReturnCreated_whenUsernameContainsDotAndUnderscore() throws Exception{
        String json = """
                {
                    "firstName" : "hung",
                    "lastName" : "nguyen",
                    "username" : "hungnguyen_.",
                    "password" : "Hung1403"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errors.username").doesNotExist());
    }

    @Test
    void shouldReturnBadRequest_whenUsernameContainsSpaces() throws Exception{
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
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception{
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
    }

    @Test
    void ShouldReturnConflict_whenEmailIsAlreadyUsed() throws Exception{
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
                .thenThrow(new RegistrationException("Email is already been used!"));

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
    void shouldReturnBadRequest_whenPhoneNumberContainsLetters() throws Exception{
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
    }

    @Test
    void shouldReturnBadRequest_whenPhoneNumberDoesNotContainTenDigits() throws Exception{
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
                .andExpect(jsonPath("$.errors.phoneNumber").value(containsString("length")));
    }

    @Test
    void shouldReturnCreated_whenPhoneNumberFormatIsCorrect() throws Exception{
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
    void shouldReturnConflict_whenPhoneNumberIsAlreadyUsed() throws Exception{
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
                .thenThrow(new RegistrationException("Phone number is already been used"));

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
