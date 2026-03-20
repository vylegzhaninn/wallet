package com.github.vylegzhaninn.wallet.account;

import com.github.vylegzhaninn.wallet.exception.InsufficientFundsException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import com.github.vylegzhaninn.wallet.user.User;
import com.github.vylegzhaninn.wallet.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        assertOwner(userId);
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
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
        assertOwner(account.getUserId());
        return account;
    }

    public Page<Account> getAll(Pageable pageable) {
        User currentUser = getCurrentUser();
        return accountRepository.findAllByUserId(currentUser.getId(), pageable);
    }

    @Transactional
    public Account deposit(AccountDto request) {
        assertOwner(request.userId());
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("User not found with id: " + request.userId());
        }

        Account account = accountRepository.findByIdForUpdate(request.id())
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + request.id()));
        if (!account.getUserId().equals(request.userId())) {
            throw new AccessDeniedException("Cannot deposit to another user's account");
        }
        account.deposit(request.amount());
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(AccountDto request) {
        assertOwner(request.userId());
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("User not found with id: " + request.userId());
        }

        Account account = accountRepository.findByIdForUpdate(request.id())
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + request.id()));

        if (!account.getUserId().equals(request.userId())) {
            throw new AccessDeniedException("Cannot withdraw from another user's account");
        }


        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        account.withdraw(request.amount());
        return accountRepository.save(account);
    }

    public void delete(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
        assertOwner(account.getUserId());
        accountRepository.deleteById(id);
    }

    private void assertOwner(Long userId) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can operate only on your own accounts");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String username = authentication.getName();
        return userRepository.findByName(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }
}
