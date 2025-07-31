package com.deepviewsystems.user_service.configs;

import com.deepviewsystems.user_service.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Componente para inicializar el LocalizedMessageService en todas las excepciones
 * que necesitan internacionalización
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExceptionInitializer implements CommandLineRunner {

    private final LocalizedMessageService messageService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Inicializando LocalizedMessageService en las excepciones");
        
        // Configurar el messageService en todas las excepciones
        UserNotFoundException.setMessageService(messageService);
        UserAlreadyExistsException.setMessageService(messageService);
        UserAccountException.setMessageService(messageService);
        GoogleAuthException.setMessageService(messageService);
        InvalidTokenException.setMessageService(messageService);
        
        log.info("LocalizedMessageService configurado exitosamente en todas las excepciones");
    }
} 