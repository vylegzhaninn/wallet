package com.github.vylegzhaninn.wallet.user;

import jakarta.validation.constraints.NotNull;

public record UserDto(
    @NotNull(message = "Name shouldn't be null")
    String name,
    @NotNull(message = "Email shouldn't be null")
    String email,
    @NotNull(message = "Password shouldn't be null")
    String password
) {
}
