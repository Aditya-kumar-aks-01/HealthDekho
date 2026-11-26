package com.healthdekho.dto.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PremiumPaymentRequest(

        @NotEmpty(message = "At least one installment payment is required")
        @Size(max = 100, message = "You can pay a maximum of 100 installments at once")
        List<@Valid InstallmentPaymentRequest> payments

) {}