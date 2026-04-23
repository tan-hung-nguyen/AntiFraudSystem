package com.tanhung.antifraudsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StolenCardResponse {

    private Long id;
    private String cardNumber;
}
