package com.tamaka.tourism.dto;

import lombok.*;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    java.util.Set<String> roles,
    String activeMode,
    String status,
    Boolean isVerified,
    OperatorProfileResponse operatorProfile
) {}