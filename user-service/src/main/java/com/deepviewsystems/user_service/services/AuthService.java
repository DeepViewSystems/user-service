package com.deepviewsystems.user_service.services;

import com.deepviewsystems.user_service.records.AuthResponse;
import com.deepviewsystems.user_service.records.GoogleLoginRequest;
import com.deepviewsystems.user_service.records.LoginRequest;
import com.deepviewsystems.user_service.records.RegisterUserRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    
    AuthResponse login(LoginRequest request);
    
    AuthResponse register(RegisterUserRequest request);
    
    AuthResponse loginWithGoogle(GoogleLoginRequest request);
    
    AuthResponse refreshToken(String refreshToken);
    
    void logout(String refreshToken);
} 