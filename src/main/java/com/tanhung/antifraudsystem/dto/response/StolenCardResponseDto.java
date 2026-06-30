package com.tanhung.antifraudsystem.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StolenCardResponseDto {

    private Long id;
    private String cardNumber;
}
