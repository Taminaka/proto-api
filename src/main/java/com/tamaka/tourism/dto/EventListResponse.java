package com.tamaka.tourism.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public record EventListResponse(
    Long id,
    String name,
    String description,
    String eventType,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String location,
    List<String> images,
    Long attractionId,
    String attractionName,
    Long commerceId,
    String commerceName
) {}