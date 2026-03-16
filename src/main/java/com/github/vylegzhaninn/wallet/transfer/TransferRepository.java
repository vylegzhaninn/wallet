package com.github.vylegzhaninn.wallet.transfer;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<TransferDto> findByFromOrTo(Long from, Long to);
}
