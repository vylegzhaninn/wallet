package com.github.vylegzhaninn.wallet.repositorytest;

import com.github.vylegzhaninn.wallet.account.Account;
import com.github.vylegzhaninn.wallet.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.liquibase.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByIdForUpdate_shouldReturnAccount() {
        Long userId = 1L;
        Account account = Account.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        account = accountRepository.save(account);

        Optional<Account> found = accountRepository.findByIdForUpdate(account.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(account.getId());
        assertThat(found.get().getUserId()).isEqualTo(userId);
    }
}
