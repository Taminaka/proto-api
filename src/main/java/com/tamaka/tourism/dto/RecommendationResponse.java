package com.tamaka.tourism.dto;

import lombok.*;

import java.util.List;

public record RecommendationResponse<T>(
    List<RecommendationItem<T>> items
) {}