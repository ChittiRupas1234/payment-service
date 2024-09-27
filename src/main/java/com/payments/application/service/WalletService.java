package com.payments.application.service;


import com.payments.application.dto.TopupRequest;
import com.payments.application.dto.TransferRequest;
import com.payments.application.dto.WithdrawRequest;
import com.payments.application.entity.Wallet;
import com.payments.application.entity.WalletTransaction;
import com.payments.application.repository.WalletRepository;
import com.payments.application.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

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

        logTransaction(null, walletId, amount, "TOPUP");
        kafkaTemplate.send("Topup",request);
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

        logTransaction(fromWalletId, toWalletId, amount, "TRANSFER");

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

        logTransaction(walletId, null, amount, "WITHDRAWAL");

        // Publish the withdrawal request JSON to the "Withdrawal" topic
        kafkaTemplate.send("Withdrawal", request);

        return updatedWallet;
    }

    private void validateToken(String token) {
        if (!tokenValidationService.validateToken(token)) {
            throw new RuntimeException("Invalid Token");
        }
    }

    private void logTransaction(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String transactionType) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setFromWalletId(fromWalletId);
        transaction.setToWalletId(toWalletId);
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(LocalDateTime.now());
        walletTransactionRepository.save(transaction);
    }
}

/*
@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    */
/*@Autowired
    private KafkaProducerService kafkaProducerService;*//*


    public Wallet createWallet(String username) {
        Wallet wallet = new Wallet();
        wallet.setUsername(username);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    public Wallet topUpWallet(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet updatedWallet = walletRepository.save(wallet);

        // Log the transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setFromWalletId(null); // Top-up doesn't have a from-wallet
        transaction.setToWalletId(walletId);
        transaction.setAmount(amount);
        transaction.setTransactionType("TOPUP");
        transaction.setTransactionDate(LocalDateTime.now());
        walletTransactionRepository.save(transaction);

        //kafkaProducerService.publishEvent("Wallet topped up: " + walletId + ", Amount: " + amount);
        return updatedWallet;
    }

    public Wallet transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        Wallet fromWallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new RuntimeException("From Wallet not found"));
        Wallet toWallet = walletRepository.findById(toWalletId).orElseThrow(() -> new RuntimeException("To Wallet not found"));

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Log the transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setFromWalletId(fromWalletId);
        transaction.setToWalletId(toWalletId);
        transaction.setAmount(amount);
        transaction.setTransactionType("TRANSFER");
        transaction.setTransactionDate(LocalDateTime.now());
        walletTransactionRepository.save(transaction);

        return fromWallet; // Return the updated wallet
    }

    public BigDecimal getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        return wallet.getBalance();
    }

    public Wallet withdraw(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet updatedWallet = walletRepository.save(wallet);

        // Log the withdrawal transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setFromWalletId(walletId);
        transaction.setToWalletId(null); // Withdrawal doesn't have a to-wallet
        transaction.setAmount(amount);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setTransactionDate(LocalDateTime.now());
        walletTransactionRepository.save(transaction);
        //kafkaProducerService.publishEvent("Wallet withdrawal: " + walletId + ", Amount: " + amount);
        return updatedWallet;
    }
}
*/

/*@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public Wallet createWallet(String username) {
        Wallet wallet = new Wallet();
        wallet.setUsername(username);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    public Wallet topUpWallet(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    public Wallet transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        Wallet fromWallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new RuntimeException("From Wallet not found"));
        Wallet toWallet = walletRepository.findById(toWalletId).orElseThrow(() -> new RuntimeException("To Wallet not found"));

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        return fromWallet; // Return the updated wallet
    }

    public BigDecimal getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        return wallet.getBalance();
    }

    public Wallet withdraw(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct the amount from the wallet's balance
        wallet.setBalance(wallet.getBalance().subtract(amount));

        // Save and return the updated wallet
        return walletRepository.save(wallet);
    }
}*/
