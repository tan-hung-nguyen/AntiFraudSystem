package com.tanhung.antifraudsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserChangeRoleRequest{
    @NotBlank(message = "Username must not be null or blank")
    private String username;
    @NotBlank(message = "Role must not be null or blank")
    private String role;
}
