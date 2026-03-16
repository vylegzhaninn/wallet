package com.github.vylegzhaninn.wallet.transfer;

import com.github.vylegzhaninn.wallet.account.Account;
import com.github.vylegzhaninn.wallet.account.AccountRepository;
import com.github.vylegzhaninn.wallet.exception.InsufficientFundsException;
import com.github.vylegzhaninn.wallet.exception.InvalidAmountException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;

    public List<TransferDto> getHistory(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Account not found");
        }

        return transferRepository.findByFromOrTo(accountId, accountId);
    }

    @Transactional
    public void transfer(TransferDto request) {
        Long firstId = Math.min(request.from(), request.to());
        Long secondId = Math.max(request.from(), request.to());

        Account first = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + firstId));
        Account second = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + secondId));

        Account from = first.getId().equals(request.from()) ? first : second;
        Account to = first.getId().equals(request.to()) ? first : second;

        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        from.withdraw(request.amount());
        to.deposit(request.amount());

        accountRepository.save(from);
        accountRepository.save(to);

        Transfer transfer = Transfer.builder()
                .from(request.from())
                .to(request.to())
                .userIdFrom(request.userIdFrom())
                .userIdTo(request.userIdTo())
                .amount(request.amount())
                .build();
        transferRepository.save(transfer);
    }
}
