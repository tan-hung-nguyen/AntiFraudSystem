package com.tanhung.antifraudsystem.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserAccessChangeRequest(@NotBlank(message = "Username must not be null or blank") String username,
                                      @NotBlank(message = "Operation must not be null or blank") String operation) {
}
