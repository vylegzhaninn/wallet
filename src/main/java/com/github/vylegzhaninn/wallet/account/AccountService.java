package com.github.vylegzhaninn.wallet.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account create(AccountDto request) {
        Account account = Account.builder()
                .userId(request.userId())
                .balance(BigDecimal.ZERO)
                .build();
        return accountRepository.save(account);
    }

    public Account deposit(AccountDto request) {
        Account account = accountRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(request.amount()));

        return accountRepository.save(account);
    }

}
