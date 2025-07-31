package com.deepviewsystems.user_service.exceptions;

import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAccountException extends RuntimeException {
    
    private static LocalizedMessageService messageService;
    
    public static void setMessageService(LocalizedMessageService messageService) {
        UserAccountException.messageService = messageService;
    }
    
    public UserAccountException(String message) {
        super(message);
    }
    
    public UserAccountException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static UserAccountException accountDisabled(String email) {
        return new UserAccountException(messageService != null ? 
            messageService.getMessage("exception.user.inactive") :
            "La cuenta del usuario " + email + " está desactivada");
    }
    
    public static UserAccountException accountLocked(String email) {
        return new UserAccountException(messageService != null ? 
            messageService.getMessage("exception.user.locked") :
            "La cuenta del usuario " + email + " está bloqueada");
    }
    
    public static UserAccountException credentialsExpired(String email) {
        return new UserAccountException(messageService != null ? 
            messageService.getMessage("auth.login.credentials.expired") :
            "Las credenciales del usuario " + email + " han expirado");
    }
} 