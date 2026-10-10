package com.tamaka.tourism.dto;

import lombok.*;

public record AccountListResponse(
    Long id,
    String email,
    String fullName,
    java.util.Set<String> roles,
    String activeMode,
    String status,
    Boolean isVerified,
    java.time.LocalDateTime createdAt
) {}