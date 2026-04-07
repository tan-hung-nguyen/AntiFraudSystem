package com.tanhung.antifraudsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonPropertyOrder({"error", "statusCode", "details", "timestamp"})
public class ErrorResponse {

    private Integer statusCode;
    private String error;
    private String details;
    private Instant timestamp;

    public ErrorResponse(Integer statusCode, String error, String details){
        this.statusCode = statusCode;
        this.error = error;
        this.details = details;
    }
}
