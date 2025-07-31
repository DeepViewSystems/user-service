package com.deepviewsystems.user_service.records;

public record GoogleUserInfo(
    String sub,           // Google User ID
    String email,
    String name,
    String picture,
    String givenName,
    String familyName
) {} 