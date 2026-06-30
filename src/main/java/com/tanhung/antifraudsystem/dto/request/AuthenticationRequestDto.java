package com.tanhung.antifraudsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AuthenticationRequestDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}

