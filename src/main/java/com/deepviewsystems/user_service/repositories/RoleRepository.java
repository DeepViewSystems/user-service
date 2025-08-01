package com.deepviewsystems.user_service.repositories;

import com.deepviewsystems.user_service.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByAuthority(String authority);
    
}
