package com.deepviewsystems.user_service.records;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "El token de Google es requerido")
    String googleToken
) {} 