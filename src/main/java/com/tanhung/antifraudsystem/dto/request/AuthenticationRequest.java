package com.tanhung.antifraudsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
