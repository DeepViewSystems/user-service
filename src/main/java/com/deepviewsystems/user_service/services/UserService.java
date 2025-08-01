package com.deepviewsystems.user_service.services;

import com.deepviewsystems.user_service.entities.User;
import com.deepviewsystems.user_service.records.RegisterUserRequest;
import com.deepviewsystems.user_service.records.UserProfileResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService extends UserDetailsService {

    Optional<User> findByEmail(String email);

    Optional<User> findActiveUserByEmail(String email);

    User createLocalUser(RegisterUserRequest request);

    User createOrUpdateGoogleUser(String email, String googleUserId);

    void activateUser(Long userId);

    void deactivateUser(Long userId);

    void lockUser(Long userId);

    void unlockUser(Long userId);

    UserProfileResponse getUserProfile(Long userId);

    void changePassword(User user, String newPassword);

    boolean hasLocalAuth(User user);
}
