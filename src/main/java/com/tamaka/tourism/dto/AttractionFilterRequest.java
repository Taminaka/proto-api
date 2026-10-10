package com.tamaka.tourism.dto;

import lombok.*;

public record AttractionFilterRequest(
    String name,
    Long categoryId,
    String status
) {}