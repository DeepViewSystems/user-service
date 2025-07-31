package com.deepviewsystems.user_service.controllers;

import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import com.deepviewsystems.user_service.records.*;
import com.deepviewsystems.user_service.services.AuthService;
import com.deepviewsystems.user_service.services.PasswordResetService;
import com.deepviewsystems.user_service.services.LoginStrategyContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints de autenticación y registro")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final LoginStrategyContext loginStrategyContext;
    private final LocalizedMessageService messageService;

    @PostMapping("/login")
    @Operation(
            summary = "Login tradicional",
            description = "Autenticación de usuario usando email y contraseña. Retorna tokens JWT para acceso y renovación."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Login exitoso",
                        "data": {
                            "userId": 1,
                            "email": "usuario@ejemplo.com",
                            "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
                            "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                            "roles": ["ROLE_USER"],
                            "isNewUser": false
                        }
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Credenciales inválidas o error de validación",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": false,
                        "message": "Credenciales inválidas",
                        "data": null
                    }
                    """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.auth.login.success"), response));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registro de usuario",
            description = "Registro de nuevo usuario con email y contraseña. El usuario se crea con rol USER por defecto."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Usuario registrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Usuario registrado exitosamente",
                        "data": {
                            "userId": 2,
                            "email": "nuevo@ejemplo.com",
                            "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
                            "refreshToken": "a12bc34d-56ef-7890-ab12-cd34ef567890",
                            "roles": ["ROLE_USER"],
                            "isNewUser": true
                        }
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Email ya existe o error de validación",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": false,
                        "message": "Ya existe un usuario con este email",
                        "data": null
                    }
                    """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.auth.register.success"), response));
    }

    @PostMapping("/google")
    @Operation(
            summary = "Login con Google OAuth",
            description = "Autenticación usando token ID de Google OAuth 2.0. Si el usuario no existe, se crea automáticamente."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login con Google exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Login con Google exitoso",
                        "data": {
                            "userId": 3,
                            "email": "usuario@gmail.com",
                            "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
                            "refreshToken": "b23cd45e-67fg-8901-bc23-de45fg678901",
                            "roles": ["ROLE_USER"],
                            "isNewUser": false
                        }
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Token de Google inválido o cuenta desactivada",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": false,
                        "message": "Token de Google inválido",
                        "data": null
                    }
                    """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.auth.google.success"), response));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refrescar token de acceso",
            description = "Obtener un nuevo access token utilizando un refresh token válido. El refresh token debe estar activo y no expirado."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refrescado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Token refrescado exitosamente",
                        "data": {
                            "userId": 1,
                            "email": "usuario@ejemplo.com",
                            "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
                            "refreshToken": "c34de56f-78gh-9012-cd34-ef56gh789012",
                            "roles": ["ROLE_USER"],
                            "isNewUser": false
                        }
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Refresh token inválido o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": false,
                        "message": "Token de refresh inválido",
                        "data": null
                    }
                    """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestParam(value = "refreshToken", required = true)
            String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.auth.refresh.success"), response));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Cerrar sesión del usuario e invalidar el refresh token para prevenir su uso futuro."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Logout exitoso",
                        "data": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Error al hacer logout",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": false,
                        "message": "Error al hacer logout",
                        "data": null
                    }
                    """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestParam(value = "refreshToken", required = true)
            String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.auth.logout.success")));
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "Solicitar reset de contraseña",
            description = "Solicitar reset de contraseña. Si el email existe y tiene autenticación local, se enviará un correo con las instrucciones.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Solicitud procesada",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Solicitud procesada. Si el email existe, recibirás un correo con las instrucciones",
                        "data": null
                    }
                    """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.auth.password.reset.request")));
    }

    @PostMapping("/password/change")
    @Operation(summary = "Cambiar contraseña", description = "Cambiar contraseña usando token de recuperación")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contraseña cambiada exitosamente",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        passwordResetService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.auth.password.change.success")));
    }

    @GetMapping("/password/validate-token")
    @Operation(summary = "Validar token de reset", description = "Verificar si un token de reset es válido")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token validado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<Boolean>> validatePasswordResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.isTokenValid(token);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.auth.password.token.valid"), isValid));
    }

    @GetMapping("/strategies")
    @Operation(
            summary = "Obtener estrategias de autenticación disponibles",
            description = "Retorna la lista de tipos de autenticación soportados por el sistema"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Estrategias disponibles",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Estrategias de autenticación disponibles",
                        "data": ["TRADITIONAL", "GOOGLE"]
                    }
                    """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<List<String>>> getAvailableStrategies() {
        java.util.List<String> strategies = loginStrategyContext.getAvailableStrategies();
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessage("api.strategies.available"), strategies));
    }

}