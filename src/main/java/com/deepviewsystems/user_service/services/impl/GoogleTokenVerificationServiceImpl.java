package com.deepviewsystems.user_service.services.impl;

import com.deepviewsystems.user_service.records.GoogleUserInfo;
import com.deepviewsystems.user_service.records.GoogleTokenInfo;
import com.deepviewsystems.user_service.services.GoogleTokenVerificationService;
import com.deepviewsystems.user_service.exceptions.GoogleAuthException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenVerificationServiceImpl implements GoogleTokenVerificationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Override
    public GoogleUserInfo verifyToken(String idToken) {
        log.info("Iniciando verificación de token de Google");
        
        try {
            String url = GOOGLE_TOKEN_INFO_URL + idToken;
            log.debug("Realizando solicitud a Google para verificar token: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Respuesta HTTP no exitosa de Google: {}", response.getStatusCode());
                throw GoogleAuthException.invalidToken("respuesta HTTP " + response.getStatusCode());
            }

            log.debug("Respuesta exitosa de Google, parseando información del token");
            GoogleTokenInfo tokenInfo = objectMapper.readValue(response.getBody(), GoogleTokenInfo.class);

            // Verificar que el token sea para nuestra aplicación (opcional)
            if (!googleClientId.isEmpty() && !googleClientId.equals(tokenInfo.aud())) {
                log.error("Token de Google no válido para esta aplicación. Expected: {}, Got: {}", 
                        googleClientId, tokenInfo.aud());
                throw GoogleAuthException.invalidToken("no válido para esta aplicación");
            }

            // Verificar que el token no haya expirado
            long currentTime = System.currentTimeMillis() / 1000;
            if (tokenInfo.exp() < currentTime) {
                log.warn("Token de Google expirado. Expiry: {}, Current: {}", tokenInfo.exp(), currentTime);
                throw GoogleAuthException.invalidToken("token expirado");
            }

            // Verificar que el email esté verificado
            if (!"true".equals(tokenInfo.email_verified())) {
                log.warn("Email de Google no verificado para usuario: {}", tokenInfo.email());
                throw GoogleAuthException.emailNotVerified();
            }

            log.info("Token de Google verificado exitosamente para usuario: {}", tokenInfo.email());
            log.debug("Información del usuario de Google: sub={}, name={}", tokenInfo.sub(), tokenInfo.name());

            return new GoogleUserInfo(
                    tokenInfo.sub(),
                    tokenInfo.email(),
                    tokenInfo.name(),
                    tokenInfo.picture(),
                    tokenInfo.given_name(),
                    tokenInfo.family_name()
            );

        } catch (GoogleAuthException e) {
            // Re-lanzar excepciones de Google Auth sin modificar
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al verificar token de Google: {}", e.getMessage(), e);
            throw GoogleAuthException.verificationFailed();
        }
    }
} 