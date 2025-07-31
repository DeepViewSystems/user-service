package com.deepviewsystems.user_service.services.impl.strategies.impl;

import com.deepviewsystems.user_service.entities.User;
import com.deepviewsystems.user_service.entities.UserAuthentication;
import com.deepviewsystems.user_service.records.AuthResponse;
import com.deepviewsystems.user_service.records.GoogleLoginRequest;
import com.deepviewsystems.user_service.records.GoogleUserInfo;
import com.deepviewsystems.user_service.services.LoginStrategy;
import com.deepviewsystems.user_service.services.UserService;
import com.deepviewsystems.user_service.services.JwtTokenService;
import com.deepviewsystems.user_service.services.GoogleTokenVerificationService;
import com.deepviewsystems.user_service.repositories.UserAuthenticationRepository;
import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import com.deepviewsystems.user_service.exceptions.UserAccountException;
import com.deepviewsystems.user_service.exceptions.GoogleAuthException;
import com.deepviewsystems.user_service.services.impl.strategies.GoogleLoginStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleLoginStrategyImpl implements GoogleLoginStrategy, LoginStrategy {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final GoogleTokenVerificationService googleTokenVerificationService;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final LocalizedMessageService messageService;

    @Override
    public AuthResponse authenticate(GoogleLoginRequest request) {
        log.info("Ejecutando estrategia de login con Google");
        
        try {
            // Verificar el token de Google
            log.debug("Verificando token de Google con el servicio de verificación");
            GoogleUserInfo googleUserInfo = googleTokenVerificationService.verifyToken(request.googleToken());
            
            log.debug("Token de Google verificado exitosamente para email: {}", googleUserInfo.email());

            // Buscar o crear usuario
            Optional<UserAuthentication> existingAuth = userAuthenticationRepository
                    .findByProviderNameAndProviderUserId("GOOGLE", googleUserInfo.sub());

            User user;
            boolean isNewUser = false;

            if (existingAuth.isPresent()) {
                // Usuario existente con Google
                log.debug("Usuario existente encontrado con autenticación de Google");
                user = existingAuth.get().getUser();
                if (!user.isEnabled()) {
                    log.warn("Intento de login con Google para cuenta desactivada: {}", user.getEmail());
                    throw UserAccountException.accountDisabled(user.getEmail());
                }
                log.debug("Usuario existente autenticado: {}", user.getEmail());
            } else {
                // Usuario nuevo o existente sin Google
                log.debug("Usuario nuevo o existente sin Google, procesando...");
                boolean userExistedBefore = userService.findByEmail(googleUserInfo.email()).isPresent();
                user = userService.createOrUpdateGoogleUser(googleUserInfo.email(), googleUserInfo.sub());
                isNewUser = !userExistedBefore;
                log.info("Usuario {} procesado para Google OAuth: {}", 
                        isNewUser ? "nuevo" : "existente", user.getEmail());
            }

            log.info("Login con Google exitoso para usuario: {} (nuevo: {})", user.getEmail(), isNewUser);
            return generateAuthResponse(user, isNewUser);

        } catch (BadCredentialsException e) {
            throw e; // Re-lanzar excepciones de credenciales tal como están
        } catch (Exception e) {
            log.error("Error inesperado al autenticar con Google: {}", e.getMessage(), e);
            throw GoogleAuthException.verificationFailed();
        }
    }

    @Override
    public AuthResponse authenticate(Object request) {
        if (!(request instanceof GoogleLoginRequest)) {
            throw new IllegalArgumentException(messageService.getMessage("exception.validation.field.invalid", "request type"));
        }
        return authenticate((GoogleLoginRequest) request);
    }

    @Override
    public String getStrategyType() {
        return "GOOGLE";
    }

    @Override
    public boolean canHandle(Object request) {
        return request instanceof GoogleLoginRequest;
    }

    private AuthResponse generateAuthResponse(User user, boolean isNewUser) {
        log.debug("Generando respuesta de autenticación para usuario: {}", user.getEmail());
        
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toSet());

        log.debug("Tokens generados exitosamente para usuario: {}", user.getEmail());
        log.info("Usuario autenticado exitosamente: {} (nuevo: {}, roles: {})", 
                user.getEmail(), isNewUser, roles);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken,
                roles,
                isNewUser
        );
    }
} 