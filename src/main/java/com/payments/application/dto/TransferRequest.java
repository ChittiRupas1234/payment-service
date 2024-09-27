package com.payments.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private String username;
}
