package com.github.vylegzhaninn.wallet.servicetest;

import com.github.vylegzhaninn.wallet.account.Account;
import com.github.vylegzhaninn.wallet.account.AccountDto;
import com.github.vylegzhaninn.wallet.account.AccountRepository;
import com.github.vylegzhaninn.wallet.account.AccountService;
import com.github.vylegzhaninn.wallet.exception.InsufficientFundsException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import com.github.vylegzhaninn.wallet.user.User;
import com.github.vylegzhaninn.wallet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword(), List.of())
        );
        when(userRepository.findByName(user.getName())).thenReturn(java.util.Optional.of(user));
    }

    @Test
    void createAccount_Success() {
        Long userId = 1L;
        User user = User.builder().id(userId).name("Max").password("pwd").email("max@example.com").build();
        authenticateAs(user);
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
        User user = User.builder().id(userId).name("Max").password("pwd").email("max@example.com").build();
        authenticateAs(user);
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
        User user = User.builder().id(userId).name("Max").password("pwd").email("max@example.com").build();
        authenticateAs(user);

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
        User user = User.builder().id(userId).name("Max").password("pwd").email("max@example.com").build();
        authenticateAs(user);

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
        User user = User.builder().id(userId).name("Max").password("pwd").email("max@example.com").build();
        authenticateAs(user);

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
    void deposit_OtherUser_AccessDenied() {
        Long ownerId = 1L;
        Long accountId = 100L;
        BigDecimal amount = BigDecimal.valueOf(50);
        AccountDto dto = new AccountDto(ownerId, accountId, amount);

        User otherUser = User.builder().id(2L).name("Other").password("pwd").email("other@example.com").build();
        authenticateAs(otherUser);

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> accountService.deposit(dto));
        verify(accountRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void deleteAccount_Success() {
        User user = User.builder().id(1L).name("Max").password("pwd").email("max@example.com").build();
        authenticateAs(user);

        Account account = Account.builder().id(1L).userId(1L).balance(BigDecimal.ZERO).build();
        when(accountRepository.findById(1L)).thenReturn(java.util.Optional.of(account));

        accountService.delete(1L);
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void deleteAccount_NotFound() {
        when(accountRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        assertThrows(NotFoundException.class, () -> accountService.delete(1L));
    }
}
