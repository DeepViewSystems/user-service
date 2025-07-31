package com.deepviewsystems.user_service.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;


    // --- FILTRO DE SEGURIDAD PARA EL AUTHORIZATION SERVER ---
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/.well-known/openid_configuration", "/jwks")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .with(new OAuth2AuthorizationServerConfigurer(), authServer ->
                        authServer.oidc(Customizer.withDefaults())
                )
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                )
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // --- FILTRO DE SEGURIDAD PARA LA APLICACIÓN Y APIs ---
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // --- Rutas Públicas ---
                        .requestMatchers(
                                "/api/auth/**",     // APIs de autenticación
                                "/api/**",          // Todas las APIs (temporalmente públicas)
                                "/swagger-ui/**",   // Swagger UI
                                "/v3/api-docs/**",  // OpenAPI docs
                                "/swagger-ui.html", // Swagger UI HTML
                                "/oauth2/**",       // Rutas OAuth2
                                "/login/oauth2/**", // Callback OAuth2
                                "/h2-console/**",   // H2 Console (solo para desarrollo)
                                "/error",           // Página de error
                                "/",                // Página de inicio
                                "/css/**",          // Recursos estáticos
                                "/js/**",           // Recursos estáticos
                                "/images/**"        // Recursos estáticos
                        ).permitAll()
                        // --- Rutas Privadas ---
                        .anyRequest().authenticated()
                )
                // Deshabilitar formulario de login por ahora
                .formLogin(form -> form.disable())
                // Deshabilitar OAuth2 por ahora
                .oauth2Login(oauth2 -> oauth2.disable())
                // Configuración del logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        // Deshabilitar CSRF para APIs y H2 Console
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
        );

        // Permitir frames para H2 Console (solo desarrollo)
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
        );

        // Configurar CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));

        return http.build();
    }


}