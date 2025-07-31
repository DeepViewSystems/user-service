package com.deepviewsystems.user_service.services.impl;

import com.deepviewsystems.user_service.entities.AuthProvider;
import com.deepviewsystems.user_service.entities.Role;
import com.deepviewsystems.user_service.entities.User;
import com.deepviewsystems.user_service.entities.UserAuthentication;
import com.deepviewsystems.user_service.records.RegisterUserRequest;
import com.deepviewsystems.user_service.records.UserProfileResponse;
import com.deepviewsystems.user_service.repositories.AuthProviderRepository;
import com.deepviewsystems.user_service.repositories.RoleRepository;
import com.deepviewsystems.user_service.repositories.UserAuthenticationRepository;
import com.deepviewsystems.user_service.repositories.UserRepository;
import com.deepviewsystems.user_service.configs.LocalizedMessageService;
import com.deepviewsystems.user_service.services.UserService;
import com.deepviewsystems.user_service.exceptions.UserNotFoundException;
import com.deepviewsystems.user_service.exceptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final AuthProviderRepository authProviderRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocalizedMessageService messageService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Buscando usuario activo por email: {}", email);
        return userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado o inactivo: {}", email);
                    return new UsernameNotFoundException(messageService.getMessage("exception.user.not.found", "email", email));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        log.debug("Usuario encontrado: {}", user.isPresent());
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findActiveUserByEmail(String email) {
        log.debug("Buscando usuario activo por email: {}", email);
        Optional<User> user = userRepository.findActiveUserByEmail(email);
        log.debug("Usuario activo encontrado: {}", user.isPresent());
        return user;
    }

    @Override
    public User createLocalUser(RegisterUserRequest request) {
        log.info("Iniciando creación de usuario local para email: {}", request.email());
        
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Intento de registro con email existente: {}", request.email());
            throw UserAlreadyExistsException.withEmail(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .accountNonLocked(true)
                .build();

        // Asignar rol por defecto
        Role defaultRole = roleRepository.findByAuthority("ROLE_USER")
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("exception.system.error")));
        user.setRoles(Set.of(defaultRole));

        User savedUser = userRepository.save(user);

        // Crear autenticación local
        AuthProvider localProvider = authProviderRepository.findByName("LOCAL")
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("exception.system.error")));

        UserAuthentication localAuth = UserAuthentication.builder()
                .user(savedUser)
                .provider(localProvider)
                .providerUserId(savedUser.getEmail())
                .build();

        userAuthenticationRepository.save(localAuth);

        log.info("Usuario local creado: {}", savedUser.getEmail());
        return savedUser;
    }

    @Override
    public User createOrUpdateGoogleUser(String email, String googleUserId) {
        log.info("Procesando usuario de Google - email: {}, googleId: {}", email, googleUserId);
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            log.debug("Usuario existente encontrado, agregando autenticación de Google");
            return addGoogleAuthToUser(existingUser.get(), googleUserId);
        } else {
            log.debug("Usuario nuevo, creando con autenticación de Google");
            return createNewGoogleUser(email, googleUserId);
        }
    }

    private User addGoogleAuthToUser(User user, String googleUserId) {
        AuthProvider googleProvider = authProviderRepository.findByName("GOOGLE")
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("exception.system.error")));

        // Verificar si ya tiene autenticación con Google
        Optional<UserAuthentication> existingGoogleAuth = 
                userAuthenticationRepository.findByUserAndProvider(user, googleProvider);

        if (existingGoogleAuth.isEmpty()) {
            UserAuthentication googleAuth = UserAuthentication.builder()
                    .user(user)
                    .provider(googleProvider)
                    .providerUserId(googleUserId)
                    .build();

            userAuthenticationRepository.save(googleAuth);
            log.info("Autenticación de Google agregada al usuario: {}", user.getEmail());
        }

        return user;
    }

    private User createNewGoogleUser(String email, String googleUserId) {
        User user = User.builder()
                .email(email)
                .password(null) // Sin contraseña para usuarios de Google
                .enabled(true)
                .accountNonLocked(true)
                .build();

        // Asignar rol por defecto
        Role defaultRole = roleRepository.findByAuthority("ROLE_USER")
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("exception.system.error")));
        user.setRoles(Set.of(defaultRole));

        User savedUser = userRepository.save(user);

        // Crear autenticación de Google
        AuthProvider googleProvider = authProviderRepository.findByName("GOOGLE")
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("exception.system.error")));

        UserAuthentication googleAuth = UserAuthentication.builder()
                .user(savedUser)
                .provider(googleProvider)
                .providerUserId(googleUserId)
                .build();

        userAuthenticationRepository.save(googleAuth);

        log.info("Usuario de Google creado: {}", savedUser.getEmail());
        return savedUser;
    }

    @Override
    public void activateUser(Long userId) {
        log.info("Iniciando activación de usuario con ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para activación - ID: {}", userId);
                    return new UserNotFoundException(userId);
                });
        
        if (user.isEnabled()) {
            log.warn("Usuario ya estaba activado - ID: {}, email: {}", userId, user.getEmail());
        }
        
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Usuario activado exitosamente - ID: {}, email: {}", userId, user.getEmail());
    }

    @Override
    public void deactivateUser(Long userId) {
        log.info("Iniciando desactivación de usuario con ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para desactivación - ID: {}", userId);
                    return new UserNotFoundException(userId);
                });
        
        if (!user.isEnabled()) {
            log.warn("Usuario ya estaba desactivado - ID: {}, email: {}", userId, user.getEmail());
        }
        
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Usuario desactivado exitosamente - ID: {}, email: {}", userId, user.getEmail());
    }

    @Override
    public void lockUser(Long userId) {
        log.info("Iniciando bloqueo de usuario con ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para bloqueo - ID: {}", userId);
                    return new UserNotFoundException(userId);
                });
        
        if (!user.isAccountNonLocked()) {
            log.warn("Usuario ya estaba bloqueado - ID: {}, email: {}", userId, user.getEmail());
        }
        
        user.setAccountNonLocked(false);
        userRepository.save(user);
        log.info("Usuario bloqueado exitosamente - ID: {}, email: {}", userId, user.getEmail());
    }

    @Override
    public void unlockUser(Long userId) {
        log.info("Iniciando desbloqueo de usuario con ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para desbloqueo - ID: {}", userId);
                    return new UserNotFoundException(userId);
                });
        
        if (user.isAccountNonLocked()) {
            log.warn("Usuario ya estaba desbloqueado - ID: {}, email: {}", userId, user.getEmail());
        }
        
        user.setAccountNonLocked(true);
        userRepository.save(user);
        log.info("Usuario desbloqueado exitosamente - ID: {}, email: {}", userId, user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        log.debug("Obteniendo perfil de usuario - ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para obtener perfil - ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        Set<String> roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toSet());

        Set<String> authProviders = userAuthenticationRepository.findByUser(user).stream()
                .map(auth -> auth.getProvider().getName())
                .collect(Collectors.toSet());

        UserProfileResponse profile = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                roles,
                authProviders,
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                user.getCreatedDate(),
                user.getLastModifiedDate()
        );
        
        log.debug("Perfil obtenido exitosamente para usuario - ID: {}, email: {}", userId, user.getEmail());
        return profile;
    }

    @Override
    public void changePassword(User user, String newPassword) {
        log.info("Iniciando cambio de contraseña para usuario: {}", user.getEmail());
        
        // Validaciones adicionales de negocio
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException(messageService.getMessage("validation.password.required"));
        }
        
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException(messageService.getMessage("validation.password.min.length"));
        }
        
        // Validar patrón de contraseña
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!newPassword.matches(passwordPattern)) {
            throw new IllegalArgumentException(messageService.getMessage("validation.password.pattern"));
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Contraseña cambiada exitosamente para usuario: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasLocalAuth(User user) {
        log.debug("Verificando autenticación local para usuario: {}", user.getEmail());
        AuthProvider localProvider = authProviderRepository.findByName("LOCAL")
                .orElse(null);
        
        if (localProvider == null) {
            log.error("Proveedor LOCAL no encontrado en la base de datos");
            return false;
        }
        
        boolean hasLocal = userAuthenticationRepository.existsByUserAndProvider(user, localProvider);
        log.debug("Usuario {} {} autenticación local", user.getEmail(), hasLocal ? "tiene" : "no tiene");
        return hasLocal;
    }
} 