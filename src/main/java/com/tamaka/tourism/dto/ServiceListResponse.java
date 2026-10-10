package com.tamaka.tourism.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record ServiceListResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer durationMinutes,
    List<String> images,
    Boolean isVisible,
    Long commerceId,
    String commerceName,
    Set<String> categoryNames
) {}