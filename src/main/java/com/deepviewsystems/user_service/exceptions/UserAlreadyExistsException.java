package com.deepviewsystems.user_service.exceptions;

import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAlreadyExistsException extends RuntimeException {
    
    private static LocalizedMessageService messageService;
    
    public static void setMessageService(LocalizedMessageService messageService) {
        UserAlreadyExistsException.messageService = messageService;
    }
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException(messageService != null ? 
            messageService.getMessage("exception.user.already.exists", "email", email) :
            "Ya existe un usuario registrado con el email: " + email);
    }
} 