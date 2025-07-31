package com.deepviewsystems.user_service.controllers;

import com.deepviewsystems.user_service.records.ApiResponse;
import com.deepviewsystems.user_service.records.UserProfileResponse;
import com.deepviewsystems.user_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Endpoints de gestión de usuarios")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Obtener perfil de usuario", description = "Obtener información del perfil de usuario")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Perfil obtenido exitosamente", profile));
    }

    @PostMapping("/{userId}/activate")
    @Operation(summary = "Activar usuario", description = "Activar cuenta de usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Usuario activado exitosamente"));
    }

    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "Desactivar usuario", description = "Desactivar cuenta de usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Usuario desactivado exitosamente"));
    }

    @PostMapping("/{userId}/lock")
    @Operation(summary = "Bloquear usuario", description = "Bloquear cuenta de usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long userId) {
        userService.lockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Usuario bloqueado exitosamente"));
    }

    @PostMapping("/{userId}/unlock")
    @Operation(summary = "Desbloquear usuario", description = "Desbloquear cuenta de usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Usuario desbloqueado exitosamente"));
    }
} 