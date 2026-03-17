package com.tanhung.antifraudsystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(@NotNull(message = "Amount can't be null!") @DecimalMin(value = "1.00",
        message = "Your transaction must be at least 1 dollar!") BigDecimal amount) {
}
