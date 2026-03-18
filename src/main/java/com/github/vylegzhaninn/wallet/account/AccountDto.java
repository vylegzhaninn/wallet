package com.github.vylegzhaninn.wallet.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AccountDto(
    @NotNull(message = "User Id shouldn't be null")
    Long userId,
    @NotNull(message = "Account Id shouldn't be null")
    Long id,
    @NotNull(message = "Amount shouldn't be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount should be more than zero")
    BigDecimal amount
) {
}
