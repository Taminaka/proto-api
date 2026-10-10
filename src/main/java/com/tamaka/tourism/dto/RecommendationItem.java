package com.tamaka.tourism.dto;

import lombok.*;

public record RecommendationItem<T>(
    T item,
    String reason,
    Double score
) {}