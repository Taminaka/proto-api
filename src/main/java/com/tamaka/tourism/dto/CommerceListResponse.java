package com.tamaka.tourism.dto;

import lombok.*;

public record CommerceListResponse(
    Long id,
    String name,
    String description,
    String address,
    Double latitude,
    Double longitude,
    String phone,
    java.util.List<String> images,
    String status,
    Long ownerId,
    String ownerName,
    java.util.Set<String> categoryNames,
    java.time.LocalDateTime createdAt
) {}