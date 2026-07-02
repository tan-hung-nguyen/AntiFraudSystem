package com.tanhung.antifraudsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"username", "status"})
@Getter
@Setter
@AllArgsConstructor
@Builder
public class DeleteStatusResponseDto {
    private String username;
    private String status;
}
