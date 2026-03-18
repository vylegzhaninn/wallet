package com.github.vylegzhaninn.wallet.account;

import com.github.vylegzhaninn.wallet.exception.InsufficientFundsException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import com.github.vylegzhaninn.wallet.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public Account create(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }

        Account account = Account.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .build();
        return accountRepository.save(account);
    }

    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
    }

    public Page<Account> getAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Transactional
    public Account deposit(AccountDto request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("User not found with id: " + request.userId());
        }

        Account account = accountRepository.findByIdForUpdate(request.id())
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + request.id()));
        account.deposit(request.amount());
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(AccountDto request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("User not found with id: " + request.userId());
        }

        Account account = accountRepository.findByIdForUpdate(request.id())
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + request.id()));


        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        account.withdraw(request.amount());
        return accountRepository.save(account);
    }

    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new NotFoundException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }
}
