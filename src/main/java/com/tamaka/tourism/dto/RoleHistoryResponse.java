package com.tamaka.tourism.dto;

import lombok.*;

public record RoleHistoryResponse(
    Long id,
    String role,
    String action,
    String changedByEmail,
    java.time.LocalDateTime changedAt
) {}