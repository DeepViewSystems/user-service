package com.deepviewsystems.user_service.exceptions;

import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserNotFoundException extends RuntimeException {
    
    private static LocalizedMessageService messageService;
    
    public static void setMessageService(LocalizedMessageService messageService) {
        UserNotFoundException.messageService = messageService;
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UserNotFoundException(Long userId) {
        super(messageService != null ? 
            messageService.getMessage("exception.user.not.found", "ID", userId.toString()) :
            "Usuario no encontrado con ID: " + userId);
    }
    
    public UserNotFoundException(String field, String value) {
        super(messageService != null ? 
            messageService.getMessage("exception.user.not.found", field, value) :
            String.format("Usuario no encontrado con %s: %s", field, value));
    }
} 