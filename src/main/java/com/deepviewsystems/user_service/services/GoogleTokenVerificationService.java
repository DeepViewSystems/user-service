package com.deepviewsystems.user_service.services;

import com.deepviewsystems.user_service.records.GoogleUserInfo;
import org.springframework.stereotype.Service;

@Service
public interface GoogleTokenVerificationService {

    GoogleUserInfo verifyToken(String idToken);
}
