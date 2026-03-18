package com.github.vylegzhaninn.wallet.servicetest;

import com.github.vylegzhaninn.wallet.account.Account;
import com.github.vylegzhaninn.wallet.account.AccountRepository;
import com.github.vylegzhaninn.wallet.exception.InsufficientFundsException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import com.github.vylegzhaninn.wallet.transfer.Transfer;
import com.github.vylegzhaninn.wallet.transfer.TransferDto;
import com.github.vylegzhaninn.wallet.transfer.TransferRepository;
import com.github.vylegzhaninn.wallet.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void getHistory_Success() {
        Long accountId = 1L;
        when(accountRepository.existsById(accountId)).thenReturn(true);
        when(transferRepository.findByFromOrTo(accountId, accountId)).thenReturn(List.of());

        List<TransferDto> history = transferService.getHistory(accountId);

        assertNotNull(history);
        verify(transferRepository).findByFromOrTo(accountId, accountId);
    }

    @Test
    void getHistory_AccountNotFound() {
        Long accountId = 1L;
        when(accountRepository.existsById(accountId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> transferService.getHistory(accountId));
        verify(transferRepository, never()).findByFromOrTo(any(), any());
    }

    @Test
    void transfer_Success() {
        Long fromId = 1L;
        Long toId = 2L;
        BigDecimal amount = BigDecimal.valueOf(50);
        TransferDto dto = new TransferDto(fromId, toId, 10L, 20L, amount);

        Account fromAccount = Account.builder().id(fromId).balance(BigDecimal.valueOf(100)).build();
        Account toAccount = Account.builder().id(toId).balance(BigDecimal.valueOf(50)).build();

        // Since we order IDs in service to avoid deadlock, we need to mock finding by ID regardless of order
        when(accountRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(toAccount));

        transferService.transfer(dto);

        assertEquals(BigDecimal.valueOf(50), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(100), toAccount.getBalance());

        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void transfer_InsufficientFunds() {
        Long fromId = 1L;
        Long toId = 2L;
        BigDecimal amount = BigDecimal.valueOf(150);
        TransferDto dto = new TransferDto(fromId, toId, 10L, 20L, amount);

        Account fromAccount = Account.builder().id(fromId).balance(BigDecimal.valueOf(100)).build();
        Account toAccount = Account.builder().id(toId).balance(BigDecimal.valueOf(50)).build();

        when(accountRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(toAccount));

        assertThrows(InsufficientFundsException.class, () -> transferService.transfer(dto));

        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void transfer_AccountNotFound() {
        Long fromId = 1L;
        Long toId = 2L;
        TransferDto dto = new TransferDto(fromId, toId, 10L, 20L, BigDecimal.TEN);

        when(accountRepository.findByIdForUpdate(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transferService.transfer(dto));
    }
}
