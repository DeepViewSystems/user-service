package com.deepviewsystems.user_service.records;

import java.util.Set;

public record AuthResponse(
    Long userId,
    String email,
    String accessToken,
    String refreshToken,
    Set<String> roles,
    boolean isNewUser
) {} 