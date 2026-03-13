package com.github.vylegzhaninn.wallet.account;

import java.math.BigDecimal;

public record AccountDto(Long userId, Long id, BigDecimal amount) {
}
