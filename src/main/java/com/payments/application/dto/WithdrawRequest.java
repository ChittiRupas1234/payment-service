package com.payments.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WithdrawRequest {
    private UUID walletId;
    private BigDecimal amount;


}
