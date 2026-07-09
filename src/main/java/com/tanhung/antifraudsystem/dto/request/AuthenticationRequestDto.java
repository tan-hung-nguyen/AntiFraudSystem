package com.tanhung.antifraudsystem.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequestDto {

    @NotBlank(message = "Username must not be blank!")
    private String username;
    @NotBlank(message = "Password must not be blank!")
    private String password;
}

