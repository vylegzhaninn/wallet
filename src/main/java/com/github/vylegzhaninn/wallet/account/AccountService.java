package com.github.vylegzhaninn.wallet.account;

import com.github.vylegzhaninn.wallet.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public Account create(AccountDto request) {
        if (!userRepository.existsById(request.userId())) {
            throw new RuntimeException("User not found with id: " + request.userId());
        }

        Account account = Account.builder()
                .userId(request.userId())
                .balance(BigDecimal.ZERO)
                .build();
        return accountRepository.save(account);
    }

    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Transactional
    public Account deposit(AccountDto request) {
        Account account = accountRepository.findByIdForUpdate(request.id())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.id()));

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }

        account.deposit(request.amount());
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(AccountDto request) {
        Account account = accountRepository.findByIdForUpdate(request.id())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.id()));

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdraw amount must be positive");
        }

        account.withdraw(request.amount());
        return accountRepository.save(account);
    }

    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }
}
