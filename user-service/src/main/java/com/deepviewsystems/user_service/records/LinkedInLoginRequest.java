package com.deepviewsystems.user_service.records;

import jakarta.validation.constraints.NotBlank;

/**
 * Record para solicitudes de login con LinkedIn OAuth
 * Ejemplo de cómo agregar una nueva estrategia de autenticación
 */
public record LinkedInLoginRequest(
    @NotBlank(message = "{validation.token.required}")
    String linkedinToken
) {
} 