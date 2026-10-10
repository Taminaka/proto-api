package com.tamaka.tourism.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public record CommerceRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 2000) String description,
    @Size(max = 500) String address,
    @DecimalMin("-90") @DecimalMax("90") Double latitude,
    @DecimalMin("-180") @DecimalMax("180") Double longitude,
    @Size(max = 50) String phone,
    @Email String email,
    @Size(max = 200) String website,
    java.util.List<@Size(max = 500) String> images,
    java.util.List<ScheduleRequest> schedules,
    java.util.Set<@NotNull Long> categoryIds
) {}