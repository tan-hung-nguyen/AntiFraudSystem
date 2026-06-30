package com.tanhung.antifraudsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IPResponseDto {
    private Long id;
    private String ipAddress;
}
