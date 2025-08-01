package com.deepviewsystems.user_service.services;

import com.deepviewsystems.user_service.records.ChangePasswordRequest;
import com.deepviewsystems.user_service.records.PasswordResetRequest;
import org.springframework.stereotype.Service;

@Service
public interface PasswordResetService {

    void requestPasswordReset(PasswordResetRequest request);

    void changePassword(ChangePasswordRequest request);

    boolean isTokenValid(String tokenValue);

}
