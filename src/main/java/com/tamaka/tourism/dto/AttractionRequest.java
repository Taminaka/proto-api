package com.tamaka.tourism.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.Set;

public record AttractionRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 5000) String description,
    @Size(max = 500) String address,
    @DecimalMin("-90") @DecimalMax("90") Double latitude,
    @DecimalMin("-180") @DecimalMax("180") Double longitude,
    List<@Size(max = 500) String> images,
    @Size(max = 2000) String accessInfo,
    @Size(max = 2000) String restrictions,
    @Min(1) Integer capacity,
    Set<@NotNull Long> categoryIds,
    Long serviceId
) {}