package com.deepviewsystems.user_service.records;

public record EmailPasswordResetRequest(
    String email,
    String resetLink,
    int expirationHours
) {} 