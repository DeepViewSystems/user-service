package com.deepviewsystems.user_service.records;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "{validation.token.required}")
    String googleToken
) {} 