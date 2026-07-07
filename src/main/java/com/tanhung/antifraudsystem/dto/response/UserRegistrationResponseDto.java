package com.tanhung.antifraudsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationResponseDto {

    private Long id;
    private String name;
    private String username;
    private String role;
    private String token;
}
