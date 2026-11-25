
package com.healthdekho.dto.customer;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
