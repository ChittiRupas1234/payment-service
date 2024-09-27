package com.payments.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WithdrawResponse {
    private UUID walletId;
    private BigDecimal balance;
    private String token;

    public WithdrawResponse(UUID walletId, BigDecimal balance, BigDecimal updatedWalletBalance, String token) {
        this.walletId = walletId;
        this.balance = balance;
        this.token = token;
    }

}
