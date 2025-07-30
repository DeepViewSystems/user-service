package com.deepviewsystems.user_service.repositories;

import com.deepviewsystems.user_service.entities.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {
    
    Optional<AuthProvider> findByName(String name);

}
