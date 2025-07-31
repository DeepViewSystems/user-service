package com.deepviewsystems.user_service.records;

import java.time.Instant;
import java.util.Set;

public record UserProfileResponse(
    Long id,
    String email,
    Set<String> roles,
    Set<String> authProviders,
    boolean enabled,
    boolean accountNonExpired,
    boolean accountNonLocked,
    boolean credentialsNonExpired,
    Instant createdDate,
    Instant lastModifiedDate
) {} 