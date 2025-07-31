package com.deepviewsystems.user_service.services.impl;

import com.deepviewsystems.user_service.entities.PasswordResetToken;
import com.deepviewsystems.user_service.entities.User;
import com.deepviewsystems.user_service.records.ChangePasswordRequest;
import com.deepviewsystems.user_service.records.PasswordResetRequest;
import com.deepviewsystems.user_service.repositories.PasswordRestTokenRepository;
import com.deepviewsystems.user_service.services.PasswordResetService;
import com.deepviewsystems.user_service.services.UserService;
import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import com.deepviewsystems.user_service.exceptions.InvalidTokenException;
import com.deepviewsystems.user_service.exceptions.UserAccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserService userService;
    private final PasswordRestTokenRepository passwordResetTokenRepository;
    private final LocalizedMessageService messageService;


    @Value("${app.password-reset.token-expiration-hours:1}")
    private int tokenExpirationHours;

    @Value("${app.frontend.password-reset-url:http://localhost:3000/reset-password}")
    private String passwordResetUrl;

    @Override
    public void requestPasswordReset(PasswordResetRequest request) {
        log.info("Solicitud de reset de contraseña recibida para email: {}", request.email());
        

        
        Optional<User> userOpt = userService.findByEmail(request.email());
        
        if (userOpt.isEmpty()) {
            // Por seguridad, no revelamos si el email existe o no
            log.warn("Solicitud de reset de contraseña para email no registrado: {}", request.email());
            // ✅ INCREMENTAR MÉTRICA DE FALLOS
            return; // Salir sin hacer nada si el usuario no existe
        }

        User user = userOpt.get();
        log.debug("Usuario encontrado para reset de contraseña: {}", user.getEmail());

        // Verificar que el usuario tiene autenticación local
        if (!userService.hasLocalAuth(user)) {
            log.warn("Solicitud de reset de contraseña para usuario sin autenticación local: {}", request.email());
            // ✅ INCREMENTAR MÉTRICA DE FALLOS
            return; // Salir sin hacer nada si no tiene auth local
        }

        // Eliminar tokens anteriores para este usuario
        log.debug("Eliminando tokens de reset anteriores para usuario: {}", user.getEmail());
        passwordResetTokenRepository.deleteByUser(user);

        // Crear nuevo token
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenExpirationHours);
        
        log.debug("Generando nuevo token de reset - Token: {}, Expiración: {}", 
                tokenValue.substring(0, 8) + "...", expiryDate);

        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(expiryDate)
                .build();

        passwordResetTokenRepository.save(token);

        log.info("Token de reset de contraseña creado exitosamente para usuario: {} (expira en {} horas)", 
                user.getEmail(), tokenExpirationHours);
        
        // Construir el link de reset
        String resetLink = passwordResetUrl + "?token=" + tokenValue;
        
        // ✅ PUBLICAR EVENTO A KAFKA (asíncrono)
        try {

            
        } catch (Exception e) {
            log.error("❌ Error al publicar evento de password reset: {}", e.getMessage(), e);
            // ✅ INCREMENTAR MÉTRICA DE EVENTOS FALLIDOS

            // No lanzamos excepción para no revelar información sensible
        }
        
        log.info("✅ Proceso de reset de contraseña completado para: {}", user.getEmail());
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        log.info("Solicitud de cambio de contraseña recibida con token: {}...", 
                request.token().substring(0, 8));
        
        // Buscar token válido
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenAndNotExpired(request.token(), LocalDateTime.now())
                .orElseThrow(() -> {
                    log.warn("Intento de cambio de contraseña con token inválido o expirado: {}...", 
                            request.token().substring(0, 8));

                    
                    return new InvalidTokenException("password reset", messageService.getMessage("auth.password.reset.token.invalid"));
                });

        User user = token.getUser();
        log.debug("Token válido encontrado para usuario: {}", user.getEmail());

        // Verificar que el usuario esté activo
        if (!user.isEnabled()) {
            log.warn("Intento de cambio de contraseña para usuario desactivado: {}", user.getEmail());
            throw UserAccountException.accountDisabled(user.getEmail());
        }

        // Cambiar contraseña
        log.debug("Cambiando contraseña para usuario: {}", user.getEmail());
        userService.changePassword(user, request.newPassword());

        // Eliminar el token usado
        log.debug("Eliminando token usado para usuario: {}", user.getEmail());
        passwordResetTokenRepository.delete(token);

        // ✅ PUBLICAR EVENTO DE CAMBIO EXITOSO


        log.info("✅ Contraseña cambiada exitosamente para usuario: {}", user.getEmail());
    }

    @Override
    public boolean isTokenValid(String tokenValue) {
        log.debug("Validando token de reset: {}...", tokenValue.substring(0, 8));
        
        Optional<PasswordResetToken> token = passwordResetTokenRepository
                .findByTokenAndNotExpired(tokenValue, LocalDateTime.now());
        
        boolean isValid = token.isPresent();
        log.debug("Token de reset {}: {}", tokenValue.substring(0, 8) + "...", 
                isValid ? "VÁLIDO" : "INVÁLIDO/EXPIRADO");
        
        return isValid;
    }


} 