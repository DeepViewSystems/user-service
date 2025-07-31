package com.deepviewsystems.user_service.repositories;

import com.deepviewsystems.user_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true AND u.accountNonLocked = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    boolean existsByEmail(String email);
}
