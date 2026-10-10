package com.tamaka.tourism.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public record EventRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 5000) String description,
    @NotNull com.tamaka.tourism.entity.EventType eventType,
    @NotNull @Future LocalDateTime startDate,
    @NotNull LocalDateTime endDate,
    @Size(max = 500) String location,
    @DecimalMin("-90") @DecimalMax("90") Double latitude,
    @DecimalMin("-180") @DecimalMax("180") Double longitude,
    List<@Size(max = 500) String> images,
    Long attractionId,
    Long commerceId
) {}