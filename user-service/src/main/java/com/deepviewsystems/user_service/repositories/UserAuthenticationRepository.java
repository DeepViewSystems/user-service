package com.deepviewsystems.user_service.repositories;

import com.deepviewsystems.user_service.entities.AuthProvider;
import com.deepviewsystems.user_service.entities.User;
import com.deepviewsystems.user_service.entities.UserAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthenticationRepository extends JpaRepository<UserAuthentication, Long> {
    
    List<UserAuthentication> findByUser(User user);
    
    Optional<UserAuthentication> findByUserAndProvider(User user, AuthProvider provider);
    
    @Query("SELECT ua FROM UserAuthentication ua WHERE ua.provider.name = :providerName AND ua.providerUserId = :providerUserId")
    Optional<UserAuthentication> findByProviderNameAndProviderUserId(@Param("providerName") String providerName, @Param("providerUserId") String providerUserId);
    
    boolean existsByUserAndProvider(User user, AuthProvider provider);
}
