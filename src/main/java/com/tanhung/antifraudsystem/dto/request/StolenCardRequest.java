package com.tanhung.antifraudsystem.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StolenCardRequest {
    @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
    @NotNull(message = "Your card number must not be null!")
    @JsonProperty(namespace = "card_number")
    private String cardNumber;
}
