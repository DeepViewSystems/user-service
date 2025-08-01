package com.deepviewsystems.user_service.strategies.strategy;

import com.deepviewsystems.user_service.records.AuthResponse;
import org.springframework.stereotype.Service;

/**
 * Interfaz del patrón Strategy para diferentes tipos de autenticación
 */
@Service
public interface LoginStrategy {

    /**
     * Ejecuta la estrategia de autenticación específica
     * @param request Objeto con los datos de autenticación
     * @return Respuesta de autenticación con tokens
     */
    AuthResponse authenticate(Object request);

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
