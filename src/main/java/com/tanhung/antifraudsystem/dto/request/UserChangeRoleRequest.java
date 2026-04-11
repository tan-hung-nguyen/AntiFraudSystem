package com.tanhung.antifraudsystem.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserChangeRoleRequest(@NotBlank(message = "Username must not be null or blank") String username,
                                    @NotBlank(message = "Role must not be null or blank") String role) {
}
