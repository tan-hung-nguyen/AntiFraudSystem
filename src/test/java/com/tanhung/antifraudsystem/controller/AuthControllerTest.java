package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponse;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    AuthService authService;

    @Test
    void shouldReturnCreated_whenRegisterSuccessfully() throws Exception {
        String json = """
                {
                    "firstName" : "hung",
                    "lastName" : "nguyen",
                    "username" : "hungnguyen",
                    "password" : "hung1403"
                }
                """;
        UserRegistrationResponse expected = new UserRegistrationResponse(1L,"hung nguyen", "hungnguyen");
        Mockito.when(authService.register(Mockito.any())).thenReturn(expected);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("hung nguyen"))
                .andExpect(jsonPath("$.username").value("hungnguyen"));

        Mockito.verify(authService).register(Mockito.any());
    }

    @Test
    void shouldReturnBadRequest_whenFirstNameIsMissing() throws Exception{
        String json = """
                {
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
                .andExpect(jsonPath("$.errors.firstName").exists());

        Mockito.verifyNoInteractions(authService);
    }
}