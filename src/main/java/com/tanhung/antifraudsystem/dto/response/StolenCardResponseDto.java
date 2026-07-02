package com.tanhung.antifraudsystem.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StolenCardResponseDto {

    private Long id;
    private String cardNumber;
}
