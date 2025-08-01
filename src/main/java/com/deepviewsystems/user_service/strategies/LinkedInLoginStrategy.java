package com.deepviewsystems.user_service.strategies;

import com.deepviewsystems.user_service.records.AuthResponse;
import com.deepviewsystems.user_service.records.LinkedInLoginRequest;
import org.springframework.stereotype.Service;

/**
 * Interfaz para la estrategia de login con LinkedIn OAuth
 */
@Service
public interface LinkedInLoginStrategy {

    /**
     * Ejecuta la autenticación con LinkedIn OAuth
     * @param request Datos de login con LinkedIn (linkedinToken)
     * @return Respuesta de autenticación con tokens
     */
    AuthResponse authenticate(LinkedInLoginRequest request);

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
