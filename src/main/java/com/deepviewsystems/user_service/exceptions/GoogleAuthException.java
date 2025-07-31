package com.deepviewsystems.user_service.exceptions;

import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleAuthException extends RuntimeException {
    
    private static LocalizedMessageService messageService;
    
    public static void setMessageService(LocalizedMessageService messageService) {
        GoogleAuthException.messageService = messageService;
    }
    
    public GoogleAuthException(String message) {
        super(message);
    }
    
    public GoogleAuthException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static GoogleAuthException invalidToken(String reason) {
        return new GoogleAuthException(messageService != null ? 
            messageService.getMessage("auth.login.google.invalid.token") + ": " + reason :
            "Token de Google inválido: " + reason);
    }
    
    public static GoogleAuthException verificationFailed() {
        return new GoogleAuthException(messageService != null ? 
            messageService.getMessage("auth.login.google.verification.failed") :
            "Falló la verificación del token de Google");
    }
    
    public static GoogleAuthException emailNotVerified() {
        return new GoogleAuthException(messageService != null ? 
            messageService.getMessage("auth.login.google.email.not.verified") :
            "El email de Google no está verificado");
    }
} 