package com.healthdekho.dto.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PremiumPaymentResponse(

        Long paymentId,
        String transactionReference,
        Long installmentId,
        Integer installmentNumber,
        BigDecimal amountPaid,
        BigDecimal penaltyAmount,
        BigDecimal outstandingAmount,
        String paymentStatus,
        OffsetDateTime paymentDate,
        String message

) {}