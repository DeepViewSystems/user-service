package com.deepviewsystems.user_service.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleTokenInfo(
    String iss,
    String sub,
    String azp,
    String aud,
    String iat,
    long exp,
    String email,
    String email_verified,
    String name,
    String picture,
    String given_name,
    String family_name,
    String locale
) {} 