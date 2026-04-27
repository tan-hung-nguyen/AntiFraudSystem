package com.tanhung.antifraudsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StolenCardResponse {

    private Long id;
    private String cardNumber;
}
