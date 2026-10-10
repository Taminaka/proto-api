package com.tamaka.tourism.dto;

import lombok.*;

public record RecommendationRequest(
    Long categoryId,
    java.time.LocalDate date
) {}