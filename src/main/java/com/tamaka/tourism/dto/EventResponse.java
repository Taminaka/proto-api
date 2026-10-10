package com.tamaka.tourism.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
    Long id,
    String name,
    String description,
    String eventType,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String location,
    Double latitude,
    Double longitude,
    List<String> images,
    String status,
    Long attractionId,
    String attractionName,
    Long commerceId,
    String commerceName,
    Long createdById,
    String createdByName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}