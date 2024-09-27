package com.payments.application.repository;

import com.payments.application.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    // Custom queries for transactions can be added here
    List<WalletTransaction> findByFromWalletId(UUID fromWalletId);

    List<WalletTransaction> findByToWalletId(UUID toWalletId);
}

