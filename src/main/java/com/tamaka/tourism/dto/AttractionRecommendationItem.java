package com.tamaka.tourism.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

public record AttractionRecommendationItem(
    Long id,
    String name,
    String description,
    String address,
    Double latitude,
    Double longitude,
    List<String> images,
    String status,
    Integer capacity,
    Integer currentOccupancy,
    String occupancyLevel,
    Set<String> categoryNames
) {}