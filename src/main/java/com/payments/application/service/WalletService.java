package com.payments.application.service;


import com.payments.application.dto.TopupRequest;
import com.payments.application.dto.TransferRequest;
import com.payments.application.dto.WithdrawRequest;
import com.payments.application.entity.Wallet;
import com.payments.application.repository.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.UUID;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TokenValidationService tokenValidationService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    //now adding to the Statement Service application using walletRepository
    @Autowired
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createWallet(String token, String username) {
        validateToken(token);

        Wallet wallet = new Wallet();
        wallet.setUsername(username);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    public Wallet topUpWallet(String token, UUID walletId, BigDecimal amount, TopupRequest request) {
        validateToken(token);

        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet updatedWallet = walletRepository.save(wallet);


        kafkaTemplate.send("Topup", request);
        return updatedWallet;
    }

    public void transfer(String token, UUID fromWalletId, UUID toWalletId, BigDecimal amount, TransferRequest request) {
        validateToken(token);

        Wallet fromWallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new RuntimeException("From Wallet not found"));
        Wallet toWallet = walletRepository.findById(toWalletId).orElseThrow(() -> new RuntimeException("To Wallet not found"));

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Publish the transfer request JSON to the "Transfer" topic
        kafkaTemplate.send("Transfer", request);

    }

    public BigDecimal getBalance(String token, UUID walletId) {
        validateToken(token);

        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        return wallet.getBalance();
    }

    public Wallet withdraw(String token, UUID walletId, BigDecimal amount, WithdrawRequest request) {
        validateToken(token);

        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet updatedWallet = walletRepository.save(wallet);


        // Publish the withdrawal request JSON to the "Withdrawal" topic
        kafkaTemplate.send("Withdrawal", request);

        return updatedWallet;
    }

    private void validateToken(String token) {
        if (!tokenValidationService.validateToken(token)) {
            throw new RuntimeException("Invalid Token");
        }
    }
}
