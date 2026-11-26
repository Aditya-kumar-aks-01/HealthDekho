package com.healthdekho.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InstallmentPaymentRequest(

        @NotNull(message = "Installment ID is required")
        @Positive(message = "Installment ID must be positive")
        Long installmentId,

        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @NotBlank(message = "Payment method is required")
        String paymentMethod

) {}