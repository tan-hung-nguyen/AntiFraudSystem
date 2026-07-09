package com.tanhung.antifraudsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonPropertyOrder({"error", "statusCode", "details", "timestamp"})
public class ErrorResponseDto {

    private Integer statusCode;
    private String error;
    private String details;
    private Instant timestamp;
}
