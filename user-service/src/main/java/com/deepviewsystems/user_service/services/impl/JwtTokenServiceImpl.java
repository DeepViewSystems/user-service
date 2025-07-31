package com.deepviewsystems.user_service.services.impl;

import com.deepviewsystems.user_service.entities.RefreshToken;
import com.deepviewsystems.user_service.entities.User;
import com.deepviewsystems.user_service.repositories.RefreshTokenRepository;
import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import com.deepviewsystems.user_service.services.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JwtTokenServiceImpl implements JwtTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final LocalizedMessageService messageService;

    @Value("${app.jwt.secret:mySecretKey}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration:86400}") // 24 horas por defecto
    private int accessTokenExpirationSeconds;

    @Value("${app.jwt.refresh-token-expiration:604800}") // 7 días por defecto
    private int refreshTokenExpirationSeconds;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public String generateAccessToken(User user) {
        log.debug("Generando access token para usuario: {}", user.getEmail());

        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenExpirationSeconds, ChronoUnit.SECONDS);

        String[] roles = user.getRoles().stream()
                .map(role -> role.getAuthority())
                .toArray(String[]::new);

        log.debug("Access token para usuario {} expirará en: {} (roles: {})",
                user.getEmail(), expiry, String.join(", ", roles));

        String token = Jwts.builder()
                .subject(user.getEmail())  // Cambio: setSubject() -> subject()
                .issuedAt(Date.from(now))  // Cambio: setIssuedAt() -> issuedAt()
                .expiration(Date.from(expiry))  // Cambio: setExpiration() -> expiration()
                .claim("userId", user.getId())
                .claim("roles", roles)
                .signWith(getSigningKey())  // Cambio: removido SignatureAlgorithm
                .compact();

        log.debug("Access token generado exitosamente para usuario: {}", user.getEmail());
        return token;
    }

    @Override
    public String generateRefreshToken(User user) {
        log.debug("Generando refresh token para usuario: {}", user.getEmail());

        String tokenValue = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(refreshTokenExpirationSeconds, ChronoUnit.SECONDS);

        log.debug("Refresh token para usuario {} expirará en: {}", user.getEmail(), expiry);

        // Eliminar tokens de refresh anteriores para este usuario
        log.debug("Eliminando refresh tokens anteriores para usuario: {}", user.getEmail());
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(expiry)
                .build();

        refreshTokenRepository.save(refreshToken);

        log.debug("Refresh token generado y guardado exitosamente para usuario: {}", user.getEmail());
        return tokenValue;
    }


    @Override
    public boolean isRefreshTokenValid(String token, User user) {
        log.debug("Validando refresh token para usuario: {}", user.getEmail());

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByTokenAndUser(token, user);

        if (refreshToken.isEmpty()) {
            log.debug("Refresh token no encontrado para usuario: {}", user.getEmail());
            return false;
        }

        RefreshToken rt = refreshToken.get();
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            log.debug("Refresh token expirado para usuario: {}, eliminando", user.getEmail());
            refreshTokenRepository.delete(rt);
            return false;
        }

        log.debug("Refresh token válido para usuario: {}", user.getEmail());
        return true;
    }

    @Override
    public String getEmailFromAccessToken(String token) {
        log.debug("Extrayendo email del access token");

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            log.debug("Email extraído del access token: {}", email);
            return email;
        } catch (Exception e) {
            log.error("Error al extraer email del access token: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String getEmailFromRefreshToken(String token) {
        log.debug("Extrayendo email del refresh token");

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);

        if (refreshToken.isEmpty()) {
            log.warn("Refresh token no encontrado en la base de datos");
            throw new RuntimeException(messageService.getMessage("auth.token.refresh.invalid"));
        }

        RefreshToken rt = refreshToken.get();
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Refresh token expirado para usuario: {}, eliminando", rt.getUser().getEmail());
            refreshTokenRepository.delete(rt);
            throw new RuntimeException(messageService.getMessage("auth.token.refresh.expired"));
        }

        String email = rt.getUser().getEmail();
        log.debug("Email extraído del refresh token: {}", email);
        return email;
    }

    @Override
    public void invalidateRefreshToken(String token) {
        log.debug("Invalidando refresh token");

        refreshTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        rt -> {
                            log.debug("Eliminando refresh token para usuario: {}", rt.getUser().getEmail());
                            refreshTokenRepository.delete(rt);
                        },
                        () -> log.debug("Refresh token no encontrado para invalidar")
                );
    }



}