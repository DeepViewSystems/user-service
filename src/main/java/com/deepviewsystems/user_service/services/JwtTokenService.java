package com.deepviewsystems.user_service.services;

import com.deepviewsystems.user_service.entities.User;
import org.springframework.stereotype.Service;

public interface JwtTokenService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);


    boolean isRefreshTokenValid(String token, User user);

    String getEmailFromAccessToken(String token);

    String getEmailFromRefreshToken(String token);

    void invalidateRefreshToken(String token);


}
