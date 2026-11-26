package com.healthdekho.dto.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentHistoryResponse(

        Long paymentId,
        String transactionReference,
        Long installmentId,
        BigDecimal amountPaid,
        BigDecimal penaltyAmount,
        String paymentMethod,
        OffsetDateTime paymentDate

) {}