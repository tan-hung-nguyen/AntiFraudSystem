package com.tanhung.antifraudsystem.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest{

        @NotNull(message = "Amount must not be null!")
        @DecimalMin(value = "1.00",
                    message = "Your transaction must be at least 1 dollar!")
        private BigDecimal amount;

        @NotNull(message = "Ip address must not be null!")
        @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$",
                message = "Your ip in wrong format!")
        private String ip;

        @NotNull(message = "Card number must not be null!")
        @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits, no spaces!")
        @JsonProperty(namespace = "card_number")
        private String number;
}
