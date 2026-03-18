package com.github.vylegzhaninn.wallet.transfer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferDto(
    @NotNull(message = "From Id shouldn't be null")
    Long from,
    @NotNull(message = "To Id shouldn't be null")
    Long to,
    @NotNull(message = "User Id From shouldn't be null")
    Long userIdFrom,
    @NotNull(message = "User Id To shouldn't be null")
    Long userIdTo,
    @NotNull(message = "Amount shouldn't be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount should be more than zero")
    BigDecimal amount
) {
}
