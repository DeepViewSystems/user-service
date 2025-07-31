package com.deepviewsystems.user_service.services.impl.strategies;

import com.deepviewsystems.user_service.records.AuthResponse;
import com.deepviewsystems.user_service.records.LoginRequest;

/**
 * Interfaz para la estrategia de login tradicional con email y contraseña
 */
public interface TraditionalLoginStrategy {
    
    /**
     * Ejecuta la autenticación tradicional con email y contraseña
     * @param request Datos de login (email, password)
     * @return Respuesta de autenticación con tokens
     */
    AuthResponse authenticate(LoginRequest request);
    
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