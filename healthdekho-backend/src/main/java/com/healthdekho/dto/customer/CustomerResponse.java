package com.healthdekho.dto.customer;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CustomerResponse(
        Long customerId,
        String fullName,
        String email,
        LocalDate dateOfBirth,
        String phone,
        String status,
        OffsetDateTime createdAt
) {}