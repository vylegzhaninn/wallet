package com.github.vylegzhaninn.wallet.servicetest;

import com.github.vylegzhaninn.wallet.account.Account;
import com.github.vylegzhaninn.wallet.account.AccountDto;
import com.github.vylegzhaninn.wallet.account.AccountRepository;
import com.github.vylegzhaninn.wallet.account.AccountService;
import com.github.vylegzhaninn.wallet.exception.InsufficientFundsException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import com.github.vylegzhaninn.wallet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_Success() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Account created = accountService.create(userId);

        assertNotNull(created);
        assertEquals(userId, created.getUserId());
        assertEquals(BigDecimal.ZERO, created.getBalance());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> accountService.create(userId));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deposit_Success() {
        Long userId = 1L;
        Long accountId = 100L;
        BigDecimal amount = BigDecimal.valueOf(50);
        AccountDto dto = new AccountDto(userId, accountId, amount);

        Account account = Account.builder()
                .id(accountId)
                .userId(userId)
                .balance(BigDecimal.valueOf(100))
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Account updated = accountService.deposit(dto);

        assertEquals(BigDecimal.valueOf(150), updated.getBalance());
        verify(accountRepository).findByIdForUpdate(accountId);
        verify(accountRepository).save(account);
    }

    @Test
    void withdraw_Success() {
        Long userId = 1L;
        Long accountId = 100L;
        BigDecimal amount = BigDecimal.valueOf(50);
        AccountDto dto = new AccountDto(userId, accountId, amount);

        Account account = Account.builder()
                .id(accountId)
                .userId(userId)
                .balance(BigDecimal.valueOf(100))
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Account updated = accountService.withdraw(dto);

        assertEquals(BigDecimal.valueOf(50), updated.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void withdraw_InsufficientFunds() {
        Long userId = 1L;
        Long accountId = 100L;
        BigDecimal amount = BigDecimal.valueOf(150);
        AccountDto dto = new AccountDto(userId, accountId, amount);

        Account account = Account.builder()
                .id(accountId)
                .userId(userId)
                .balance(BigDecimal.valueOf(100))
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () -> accountService.withdraw(dto));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deleteAccount_Success() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        accountService.delete(1L);
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void deleteAccount_NotFound() {
        when(accountRepository.existsById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> accountService.delete(1L));
    }
}
