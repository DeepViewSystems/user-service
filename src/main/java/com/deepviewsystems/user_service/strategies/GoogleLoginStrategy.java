package com.deepviewsystems.user_service.strategies;

import com.deepviewsystems.user_service.records.AuthResponse;
import com.deepviewsystems.user_service.records.GoogleLoginRequest;
import org.springframework.stereotype.Service;

/**
 * Interfaz para la estrategia de login con Google OAuth
 */
@Service
public interface GoogleLoginStrategy {
    /**
     * Ejecuta la autenticación con Google OAuth
     * @param request Datos de login con Google (googleToken)
     * @return Respuesta de autenticación con tokens
     */
    AuthResponse authenticate(GoogleLoginRequest request);

    /**
     * Obtiene el tipo de estrategia
     * @return Nombre del tipo de autenticación
     */
    String getStrategyType();

    /**
     * Verifica si esta estrategia puede manejar el tipo de request
     * @param request Objeto con los datos de autenticación
     * @return true si puede manejar el request
     */
    boolean canHandle(Object request);
}
