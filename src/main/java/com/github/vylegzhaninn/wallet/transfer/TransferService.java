package com.github.vylegzhaninn.wallet.transfer;

import com.github.vylegzhaninn.wallet.account.AccountDto;
import com.github.vylegzhaninn.wallet.account.AccountRepository;
import com.github.vylegzhaninn.wallet.account.AccountService;
import com.github.vylegzhaninn.wallet.exception.InvalidAmountException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @Transactional
    public void transfer(TransferDto request) {
        if (!accountRepository.existsById(request.from()) || !accountRepository.existsById(request.to()))
            throw new NotFoundException("Account not found");

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("Amount should be bigger then zero");
        }

        Transfer transfer = Transfer
            .builder()
            .from(request.from())
            .to(request.to())
            .amount(request.amount())
            .build();

        transferRepository.save(transfer);

        AccountDto accountFrom = new AccountDto(
            request.userIdFrom(), request.from(), request.amount()
        );

        AccountDto accountTo = new AccountDto(
            request.userIdTo(), request.to(), request.amount()
        );

        accountService.withdraw(accountFrom);
        accountService.deposit(accountTo);
    }
}
