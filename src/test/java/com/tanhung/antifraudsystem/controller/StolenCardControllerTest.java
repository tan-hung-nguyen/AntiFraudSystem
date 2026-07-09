package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.config.JwtAuthenticationFilter;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponseDto;
import com.tanhung.antifraudsystem.exception.InvalidCardNumberException;
import com.tanhung.antifraudsystem.exception.StolenCardConflictException;
import com.tanhung.antifraudsystem.exception.StolenCardNullException;
import com.tanhung.antifraudsystem.exception.StolenCardNumberNotFoundException;
import com.tanhung.antifraudsystem.exceptionHandler.GlobalExceptionHandler;
import com.tanhung.antifraudsystem.service.StolenCardAdminService;
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

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StolenCardController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class StolenCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StolenCardAdminService stolenCardAdminService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("POST /api/antifraud/stolencard")
    class addStolenCardNumberTest {

        @Test
        @DisplayName("Return \"Created 201\" when card number is valid")
        void shouldReturnCreated_whenCardNumberIsValid() throws Exception {
            String json = """
                    {
                        "cardNumber" : "4000008449433403"
                    }
                    """;

            Mockito.when(stolenCardAdminService.addStolenCardNumber(Mockito.any()))
                    .thenReturn(new StolenCardResponseDto(1L, "4000008449433403"));

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.cardNumber").value("4000008449433403"));

            Mockito.verify(stolenCardAdminService).addStolenCardNumber(Mockito.any());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber is missing")
        void shouldReturnBadRequest_whenCardNumberIsMissing() throws Exception {
            String json = """
                    {
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber is blank")
        void shouldReturnBadRequest_whenCardNumberIsBlank() throws Exception {
            String json = """
                    {
                        "cardNumber" : ""
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber has fewer than 16 digits")
        void shouldReturnBadRequest_whenCardNumberIsTooShort() throws Exception {
            String json = """
                    {
                        "cardNumber" : "123456789"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber has more than 16 digits")
        void shouldReturnBadRequest_whenCardNumberIsTooLong() throws Exception {
            String json = """
                    {
                        "cardNumber" : "40000084494334031234"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber contains letters")
        void shouldReturnBadRequest_whenCardNumberContainsLetters() throws Exception {
            String json = """
                    {
                        "cardNumber" : "4000abcd49433403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when cardNumber contains spaces")
        void shouldReturnBadRequest_whenCardNumberContainsSpaces() throws Exception {
            String json = """
                    {
                        "cardNumber" : "4000 0084 4943 3403"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cardNumber").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request body is empty")
        void shouldReturnBadRequest_whenRequestBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists()).andDo(print());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request body is malformed JSON")
        void shouldReturnBadRequest_whenRequestBodyIsMalformedJson() throws Exception {
            String json = """
                    {
                        "cardNumber" : "4000008449433403"
                    """;

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.details").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when request contains an unknown field")
        void shouldReturnBadRequest_whenRequestContainsUnknownField() throws Exception {
            String json = """
                    {
                        "cardNumber" : "4000008449433403",
                        "extraField" : "unexpected"
                    }
                    """;

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").value(containsString("Unknown field")))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and details when service throws InvalidCardNumberException")
        void shouldReturnBadRequest_whenServiceThrowsInvalidCardNumberException() throws Exception {
            String json = """
                    {
                        "cardNumber" : "4000008449433401"
                    }
                    """;

            Mockito.when(stolenCardAdminService.addStolenCardNumber(Mockito.any()))
                    .thenThrow(new InvalidCardNumberException("4000008449433401 card number is invalid!"));

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Return \"Conflict 409\" when service throws StolenCardConflictException")
        void shouldReturnConflict_whenServiceThrowsStolenCardConflictException() throws Exception {
            String json = """
                    {
                        "cardNumber" : "4000008449433403"
                    }
                    """;

            Mockito.when(stolenCardAdminService.addStolenCardNumber(Mockito.any()))
                    .thenThrow(new StolenCardConflictException("4000008449433403 is existed in the list"));

            mockMvc.perform(post("/api/antifraud/stolencard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.statusCode").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /api/antifraud/stolencard/{number}")
    class deleteStolenCardNumberTest {

        @Test
        @DisplayName("Return \"Ok 200\" when card number is deleted successfully")
        void shouldReturnOk_whenDeletingStolenCardNumberSuccessfully() throws Exception {
            Mockito.when(stolenCardAdminService.deleteStolenCardNumber(Mockito.any()))
                    .thenReturn(new StatusResponseDto("Card 4000008449433403 successfully removed!"));

            mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").exists());

            Mockito.verify(stolenCardAdminService).deleteStolenCardNumber("4000008449433403");
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number has fewer than 16 digits")
        void shouldReturnBadRequest_whenCardNumberIsTooShort() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/123456789")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number has more than 16 digits")
        void shouldReturnBadRequest_whenCardNumberIsTooLong() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/40000084494334031234")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number contains letters")
        void shouldReturnBadRequest_whenCardNumberContainsLetters() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/4000abcd49433403")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number contains spaces")
        void shouldReturnBadRequest_whenCardNumberContainsSpaces() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/4000 0084 4943 3403")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when card number contains special characters")
        void shouldReturnBadRequest_whenCardNumberContainsSpecialCharacters() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/4000-0084-4943-3403")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" when path variable is blank")
        void shouldReturnBadRequest_whenPathVariableIsBlank() throws Exception {
            mockMvc.perform(delete("/api/antifraud/stolencard/     ")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            Mockito.verifyNoInteractions(stolenCardAdminService);
        }


        @Test
        @DisplayName("Return \"Not Found 404\" and details when service throws StolenCardNumberNotFoundException")
        void shouldReturnNotFound_whenServiceThrowsStolenCardNumberNotFoundException() throws Exception {
            Mockito.when(stolenCardAdminService.deleteStolenCardNumber(Mockito.any()))
                    .thenThrow(new StolenCardNumberNotFoundException("Card number not found!"));

            mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Return \"Bad Request 400\" and details when service throws InvalidCardNumberException")
        void shouldReturnBadRequest_whenServiceThrowsInvalidCardNumberException() throws Exception {
            Mockito.when(stolenCardAdminService.deleteStolenCardNumber(Mockito.any()))
                    .thenThrow(new InvalidCardNumberException("4000008449433403 card number is invalid!"));

            mockMvc.perform(delete("/api/antifraud/stolencard/4000008449433403")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/antifraud/stolencard")
    class getStolenCardListTest {

        @Test
        @DisplayName("Return \"Ok 200\" and list of stolen card numbers when list is not empty")
        void shouldReturnOkAndCardNumberList_whenListIsNotEmpty() throws Exception {
            Mockito.when(stolenCardAdminService.getStolenCardList())
                    .thenReturn(List.of(new StolenCardResponseDto(1L, "4000008449433403"),
                            new StolenCardResponseDto(2L, "5000008449433403")));

            mockMvc.perform(get("/api/antifraud/stolencard")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].cardNumber").exists())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Return \"Ok 200\" and empty list when no stolen card numbers exist")
        void shouldReturnOkAndEmptyList_whenListIsEmpty() throws Exception {
            Mockito.when(stolenCardAdminService.getStolenCardList())
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/antifraud/stolencard")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            Mockito.verify(stolenCardAdminService).getStolenCardList();
        }
    }
}