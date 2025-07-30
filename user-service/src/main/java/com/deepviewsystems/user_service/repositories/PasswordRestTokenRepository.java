package com.deepviewsystems.user_service.repositories;

import com.deepviewsystems.user_service.entities.PasswordResetToken;
import com.deepviewsystems.user_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordRestTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token AND prt.expiryDate > :currentTime")
    Optional<PasswordResetToken> findByTokenAndNotExpired(@Param("token") String token, @Param("currentTime") LocalDateTime currentTime);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.user = :user")
    void deleteByUser(@Param("user") User user);
}
