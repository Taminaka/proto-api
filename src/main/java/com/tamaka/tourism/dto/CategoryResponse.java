package com.tamaka.tourism.dto;

import lombok.*;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    String status
) {}