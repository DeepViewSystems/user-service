package com.deepviewsystems.user_service.exceptions;

import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvalidTokenException extends RuntimeException {

    private static LocalizedMessageService messageService;

    public static void setMessageService(LocalizedMessageService messageService) {
        InvalidTokenException.messageService = messageService;
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTokenException(String tokenType, String reason) {
        super(messageService != null ?
                messageService.getMessage("auth.token.invalid", tokenType, reason) :
                String.format("Token %s inválido: %s", tokenType, reason));
    }
}
