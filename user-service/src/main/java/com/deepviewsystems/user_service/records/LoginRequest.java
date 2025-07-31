package com.deepviewsystems.user_service.records;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    String email,
    
    @NotBlank(message = "La contraseña es requerida")
    String password
) {} 