package com.deepviewsystems.user_service.records;

public record PasswordResetTokenResponse(
    String token,
    String email,
    String resetLink,
    int expirationHours
) {} 