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
