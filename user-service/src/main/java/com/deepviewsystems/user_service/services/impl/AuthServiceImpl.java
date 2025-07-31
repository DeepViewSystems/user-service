package com.deepviewsystems.user_service.services.impl;

import com.deepviewsystems.user_service.entities.User;
import com.deepviewsystems.user_service.records.AuthResponse;
import com.deepviewsystems.user_service.records.LoginRequest;
import com.deepviewsystems.user_service.records.RegisterUserRequest;
import com.deepviewsystems.user_service.records.GoogleLoginRequest;
import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import com.deepviewsystems.user_service.services.AuthService;
import com.deepviewsystems.user_service.services.LoginStrategyContext;
import com.deepviewsystems.user_service.services.JwtTokenService;
import com.deepviewsystems.user_service.services.UserService;
import com.deepviewsystems.user_service.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final LoginStrategyContext loginStrategyContext;
    private final JwtTokenService jwtTokenService;
    private final LocalizedMessageService messageService;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Iniciando login tradicional para email: {}", request.email());
        return loginStrategyContext.authenticate(request);
    }

    @Override
    public AuthResponse register(RegisterUserRequest request) {
        log.info("Iniciando registro de nuevo usuario para email: {}", request.email());
        
        try {
            User user = userService.createLocalUser(request);
            log.info("Usuario registrado exitosamente: {}", user.getEmail());
            return generateAuthResponse(user, true);
        } catch (Exception e) {
            log.error("Error al registrar usuario {}: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        log.info("Iniciando login con Google");
        return loginStrategyContext.authenticate(request);
    }



    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Iniciando renovación de token");
        
        try {
            log.debug("Extrayendo email del refresh token");
            String email = jwtTokenService.getEmailFromRefreshToken(refreshToken);
            
            log.debug("Buscando usuario activo para email: {}", email);
            User user = userService.findActiveUserByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Usuario no encontrado o inactivo para renovación de token: {}", email);
                        return new UserNotFoundException("email", email);
                    });

            log.debug("Validando refresh token para usuario: {}", user.getEmail());
            if (!jwtTokenService.isRefreshTokenValid(refreshToken, user)) {
                log.warn("Refresh token inválido para usuario: {}", user.getEmail());
                throw new BadCredentialsException(messageService.getMessage("auth.token.refresh.invalid"));
            }

            log.info("Token renovado exitosamente para usuario: {}", user.getEmail());
            return generateAuthResponse(user, false);

        } catch (BadCredentialsException e) {
            throw e; // Re-lanzar excepciones de credenciales tal como están
        } catch (Exception e) {
            log.error("Error inesperado al refrescar token: {}", e.getMessage(), e);
            throw new BadCredentialsException(messageService.getMessage("auth.token.refresh.invalid"));
        }
    }

    @Override
    public void logout(String refreshToken) {
        log.info("Iniciando logout");
        
        try {
            jwtTokenService.invalidateRefreshToken(refreshToken);
            log.info("Logout completado exitosamente");
        } catch (Exception e) {
            log.warn("Error al hacer logout: {}", e.getMessage(), e);
            // No re-lanzamos la excepción ya que el logout debería ser "best effort"
        }
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