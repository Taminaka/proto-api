package com.tamaka.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public record AttractionStatusUpdateRequest(
    @NotBlank String status
) {}