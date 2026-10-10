package com.tamaka.tourism.dto;

import lombok.*;

public record CommerceResponse(
    Long id,
    String name,
    String description,
    String address,
    Double latitude,
    Double longitude,
    String phone,
    String email,
    String website,
    java.util.List<String> images,
    java.util.List<ScheduleRequest> schedules,
    String status,
    Long ownerId,
    String ownerName,
    java.util.Set<Long> categoryIds,
    java.util.Set<String> categoryNames,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {}