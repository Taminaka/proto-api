package com.tamaka.tourism.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record ServiceRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 2000) String description,
    @DecimalMin("0") BigDecimal price,
    @Min(1) Integer durationMinutes,
    List<@Size(max = 500) String> images,
    Set<@NotNull Long> categoryIds,
    Boolean isVisible
) {}