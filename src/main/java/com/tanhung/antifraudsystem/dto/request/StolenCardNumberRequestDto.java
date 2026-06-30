package com.tanhung.antifraudsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
public class StolenCardNumberRequestDto {
    @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
    @NotNull(message = "Your card number must not be null!")
    private String cardNumber;
}
