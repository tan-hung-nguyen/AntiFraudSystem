package com.tanhung.antifraudsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class IPResponseDto {
    private Long id;
    private String ipAddress;
}
