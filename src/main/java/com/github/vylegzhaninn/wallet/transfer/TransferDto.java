package com.github.vylegzhaninn.wallet.transfer;

import java.math.BigDecimal;

public record TransferDto(Long from, Long to,Long userIdFrom, Long userIdTo, BigDecimal amount) {
}
