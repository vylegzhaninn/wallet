package com.github.vylegzhaninn.wallet.repositorytest;

import com.github.vylegzhaninn.wallet.transfer.Transfer;
import com.github.vylegzhaninn.wallet.transfer.TransferDto;
import com.github.vylegzhaninn.wallet.transfer.TransferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.liquibase.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class TransferRepositoryTest {

    @Autowired
    private TransferRepository transferRepository;

    @Test
    void findByFromOrTo_shouldReturnTransfersWhereAccountIsSenderOrReceiver() {
        // Given
        Long accountId = 1L;
        Long otherAccountId = 2L;

        Transfer outgoing = Transfer.builder()
                .from(accountId)
                .to(otherAccountId)
                .userIdFrom(10L)
                .userIdTo(20L)
                .amount(BigDecimal.TEN)
                .build();

        Transfer incoming = Transfer.builder()
                .from(otherAccountId)
                .to(accountId)
                .userIdFrom(20L)
                .userIdTo(10L)
                .amount(BigDecimal.valueOf(20))
                .build();

        Transfer unrelated = Transfer.builder()
                .from(otherAccountId)
                .to(3L)
                .userIdFrom(20L)
                .userIdTo(30L)
                .amount(BigDecimal.valueOf(5))
                .build();

        transferRepository.save(outgoing);
        transferRepository.save(incoming);
        transferRepository.save(unrelated);

        // When
        List<TransferDto> result = transferRepository.findByFromOrTo(accountId, accountId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(TransferDto::amount)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactlyInAnyOrder(BigDecimal.TEN, BigDecimal.valueOf(20));

        // Check mapping
        TransferDto outgoingDto = result.stream()
            .filter(t -> t.amount().compareTo(BigDecimal.TEN) == 0)
            .findFirst()
            .orElseThrow();

        assertThat(outgoingDto.from()).isEqualTo(accountId);
        assertThat(outgoingDto.to()).isEqualTo(otherAccountId);
    }
}
