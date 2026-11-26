package com.healthdekho.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public record InstallmentScheduleResponse(

        Long installmentId,
        Integer installmentNumber,
        YearMonth paymentPeriod,
        BigDecimal scheduledAmount,
        BigDecimal paidAmount,
        BigDecimal penaltyAmount,
        BigDecimal outstandingAmount,
        LocalDate dueDate,
        LocalDate gracePeriodEndDate,
        String paymentStatus

) {}