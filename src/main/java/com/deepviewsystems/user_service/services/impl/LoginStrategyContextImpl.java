package com.deepviewsystems.user_service.services.impl;

import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import com.deepviewsystems.user_service.records.AuthResponse;
import com.deepviewsystems.user_service.services.LoginStrategy;
import com.deepviewsystems.user_service.services.LoginStrategyContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del contexto del patrón Strategy que maneja diferentes tipos de autenticación
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginStrategyContextImpl implements LoginStrategyContext {

    private final List<LoginStrategy> strategies;
    private final LocalizedMessageService messageService;

    @Override
    public AuthResponse authenticate(Object request) {
        log.debug(messageService.getMessage("strategy.context.searching"), 
                request != null ? request.getClass().getSimpleName() : "null");
        
        LoginStrategy strategy = findStrategy(request);
        
        if (strategy == null) {
            log.error(messageService.getMessage("strategy.context.not.found"), 
                    request != null ? request.getClass().getSimpleName() : "null");
            throw new IllegalArgumentException(messageService.getMessage("strategy.context.unsupported"));
        }
        
        log.info(messageService.getMessage("strategy.context.executing"), strategy.getStrategyType());
        return strategy.authenticate(request);
    }

    /**
     * Encuentra la estrategia apropiada para el tipo de request
     * @param request Objeto con los datos de autenticación
     * @return Estrategia apropiada o null si no se encuentra
     */
    private LoginStrategy findStrategy(Object request) {
        if (request == null) {
            return null;
        }
        
        return strategies.stream()
                .filter(strategy -> strategy.canHandle(request))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> getAvailableStrategies() {
        return strategies.stream()
                .map(LoginStrategy::getStrategyType)
                .toList();
    }
} 