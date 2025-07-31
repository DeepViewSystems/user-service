package com.deepviewsystems.user_service.services.impl.strategies.impl;

import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import com.deepviewsystems.user_service.entities.User;
import com.deepviewsystems.user_service.records.AuthResponse;
import com.deepviewsystems.user_service.records.LoginRequest;
import com.deepviewsystems.user_service.services.LoginStrategy;
import com.deepviewsystems.user_service.services.UserService;
import com.deepviewsystems.user_service.services.JwtTokenService;
import com.deepviewsystems.user_service.services.impl.strategies.TraditionalLoginStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TraditionalLoginStrategyImpl implements TraditionalLoginStrategy, LoginStrategy {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final LocalizedMessageService messageService;

    @Override
    public AuthResponse authenticate(LoginRequest request) {
        log.info(messageService.getMessage("strategy.traditional.executing"), request.email());
        
        try {
            // Autenticar usando Spring Security
            log.debug(messageService.getMessage("log.debug.auth.credentials"));
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = (User) authentication.getPrincipal();
            log.debug(messageService.getMessage("log.debug.auth.success"), user.getEmail());
            
            // Verificar si el usuario tiene autenticación local
            if (!userService.hasLocalAuth(user)) {
                log.error(messageService.getMessage("strategy.traditional.no.local.auth"), user.getEmail());
                throw new BadCredentialsException(messageService.getMessage("auth.login.no.local.auth"));
            }

            log.info(messageService.getMessage("strategy.traditional.success"), user.getEmail());
            return generateAuthResponse(user, false);

        } catch (AuthenticationException e) {
            log.warn(messageService.getMessage("log.warn.auth.failed"), request.email(), e.getMessage());
            throw new BadCredentialsException(messageService.getMessage("auth.login.invalid.credentials"));
        }
    }

    @Override
    public AuthResponse authenticate(Object request) {
        if (!(request instanceof LoginRequest)) {
            throw new IllegalArgumentException(messageService.getMessage("exception.validation.field.invalid", "request type"));
        }
        return authenticate((LoginRequest) request);
    }

    @Override
    public String getStrategyType() {
        return "TRADITIONAL";
    }

    @Override
    public boolean canHandle(Object request) {
        return request instanceof LoginRequest;
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