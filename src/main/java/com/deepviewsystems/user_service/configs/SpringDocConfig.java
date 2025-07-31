package com.deepviewsystems.user_service.configs;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Value("${app.url:http://localhost:8085}")
    private String url;

    @Value("${app.organization:Deep View Systems}")
    private String organization;

    @Value("${app.contact-email:dvs@dvs.com}")
    private String contactEmail;

    @Bean
    public OpenAPI openApi(
            @Value("${app.name:User-Service}") String appName,
            @Value("${app.desc:User Service API developed by Deep View Systems}") String appDescription,
            @Value("${app.version:1.0.0}") String appVersion) {

        Info info = new Info()
                .title(appName)
                .version(appVersion)
                .description(appDescription)
                .contact(new Contact()
                        .name(organization)
                        .email(contactEmail));

        Server server = new Server()
                .url(url)
                .description(appDescription);

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .info(info)
                .addServersItem(server);
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }
}