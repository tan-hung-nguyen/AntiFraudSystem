package com.tanhung.antifraudsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
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
