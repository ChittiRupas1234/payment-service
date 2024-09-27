package com.payments.application.controller;

import com.payments.application.dto.*;
import com.payments.application.entity.Wallet;
//import com.payments.application.service.AuthenticatedService;
import com.payments.application.service.TokenValidationService;
import com.payments.application.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@RequestHeader("Authorization") String token, @RequestBody CreateWalletRequest request) {
        Wallet newWallet = walletService.createWallet(token, request.getUsername());
        return ResponseEntity.ok(new CreateWalletResponse(newWallet.getWalletId(), token, newWallet.getBalance()));
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topUpWallet(@RequestHeader("Authorization") String token, @RequestBody TopupRequest request) {
        Wallet updatedWallet = walletService.topUpWallet(token, request.getWalletId(), request.getAmount(),request);
        return ResponseEntity.ok(new TopupResponse(updatedWallet.getWalletId(), updatedWallet.getBalance(), token));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestHeader("Authorization") String token, @RequestBody TransferRequest request) {
        walletService.transfer(token, request.getFromWalletId(), request.getToWalletId(), request.getAmount(), request);
        return ResponseEntity.ok(new TransferResponse(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount(),
                request.getUsername(),
                token
        ));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String token, @RequestParam UUID walletId) {
        BigDecimal balance = walletService.getBalance(token, walletId);
        return ResponseEntity.ok(new BalanceResponse(walletId, balance));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String token, @RequestBody WithdrawRequest request) {
        Wallet updatedWallet = walletService.withdraw(token, request.getWalletId(), request.getAmount(), request);
        return ResponseEntity.ok(new WithdrawResponse(
                request.getWalletId(),
                request.getAmount(),
                updatedWallet.getBalance(),
                token
        ));
    }
}

/*@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private TokenValidationService tokenValidationService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    *//*private static final String TRANSFER_TOPIC = "Transfer";
    private static final String WITHDRAWAL_TOPIC = "Withdrawal";*//*

    *//*@Autowired
    private AuthenticatedService authenticatedService;*//*

    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@RequestHeader("Authorization") String token, @RequestBody CreateWalletRequest request) {
        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        Wallet newWallet = walletService.createWallet(request.getUsername());
        return ResponseEntity.ok(new CreateWalletResponse(newWallet.getWalletId(), token,newWallet.getBalance()));
    }
    @PostMapping("/topup")
    public ResponseEntity<?> topUpWallet(@RequestHeader("Authorization") String token, @RequestBody


    TopUpRequest request) {
        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        Wallet updatedWallet = walletService.topUpWallet(request.getWalletId(), request.getAmount());
        return ResponseEntity.ok(new TopUpResponse(updatedWallet.getWalletId(), updatedWallet.getBalance(), token));
    }

    *//*@PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestHeader("Authorization") String token, @RequestBody TransferRequest request) {
        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        Wallet updatedFromWallet = walletService.transfer(request.getFromWalletId(), request.getToWalletId(), request.getAmount());
        return ResponseEntity.ok(new TransferResponse(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount(),
                request.getUsername(),
                token
        ));
    }*//*
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestHeader("Authorization") String token, @RequestBody TransferRequest request) {
        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        Wallet updatedFromWallet = walletService.transfer(request.getFromWalletId(), request.getToWalletId(), request.getAmount());

        // Publish the transfer request JSON to the "Transfer" topic
        kafkaTemplate.send("Transfer", request);

        return ResponseEntity.ok(new TransferResponse(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount(),
                request.getUsername(),
                token
        ));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String token, @RequestParam UUID walletId) {
        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        BigDecimal balance = walletService.getBalance(walletId);
        return ResponseEntity.ok(new BalanceResponse(walletId, balance));
    }

    *//*@PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String token, @RequestBody WithdrawalRequest request) {
        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        try {
            Wallet updatedWallet = walletService.withdraw(request.getWalletId(), request.getAmount());
            return ResponseEntity.ok(new WithdrawalResponse(updatedWallet.getWalletId(), updatedWallet.getBalance(), token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }*//*
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String token, @RequestBody WithdrawRequest request) {
        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        Wallet updatedWallet = walletService.withdraw(request.getWalletId(), request.getAmount());

        // Publish the withdrawal request JSON to the "Withdrawal" topic
        kafkaTemplate.send("Withdrawal", request);

        return ResponseEntity.ok(new WithdrawResponse(
                request.getWalletId(),
                request.getAmount(),
                updatedWallet.getBalance(),
                token
        ));
    }
}*/
