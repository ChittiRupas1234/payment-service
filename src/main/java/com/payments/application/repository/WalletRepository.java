package com.payments.application.repository;

import com.payments.application.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findById(UUID walletId);
    // You can define custom queries if needed
    Optional<Wallet> findByUsername(String username);
}
