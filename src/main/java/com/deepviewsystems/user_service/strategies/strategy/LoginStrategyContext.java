package com.deepviewsystems.user_service.strategies.strategy;

import com.deepviewsystems.user_service.records.AuthResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Interfaz del contexto del patrón Strategy que maneja diferentes tipos de autenticación
 */
@Service
public interface LoginStrategyContext {

    /**
     * Ejecuta la estrategia de autenticación apropiada basada en el tipo de request
     * @param request Objeto con los datos de autenticación
     * @return Respuesta de autenticación con tokens
     */
    AuthResponse authenticate(Object request);

    /**
     * Obtiene todas las estrategias disponibles
     * @return Lista de tipos de estrategias disponibles
     */
    List<String> getAvailableStrategies();
}
