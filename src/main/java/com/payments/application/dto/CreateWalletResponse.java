package com.payments.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletResponse {
    private UUID walletId;
    private String token;
    private BigDecimal balance;
}
