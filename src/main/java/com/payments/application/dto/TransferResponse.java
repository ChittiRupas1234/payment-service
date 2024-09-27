package com.payments.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class TransferResponse {
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private String username;
    private String token;
}
