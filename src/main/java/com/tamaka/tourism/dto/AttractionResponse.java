package com.tamaka.tourism.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

public record AttractionResponse(
    Long id,
    String name,
    String description,
    String address,
    Double latitude,
    Double longitude,
    List<String> images,
    String status,
    String accessInfo,
    String restrictions,
    Integer capacity,
    Integer currentOccupancy,
    String occupancyLevel,
    Set<Long> categoryIds,
    Set<String> categoryNames,
    Long serviceId,
    String serviceName,
    Long createdById,
    String createdByName,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {}