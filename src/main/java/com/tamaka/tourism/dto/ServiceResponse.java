package com.tamaka.tourism.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record ServiceResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer durationMinutes,
    List<String> images,
    String status,
    Boolean isVisible,
    Long commerceId,
    String commerceName,
    Set<Long> categoryIds,
    Set<String> categoryNames,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {}