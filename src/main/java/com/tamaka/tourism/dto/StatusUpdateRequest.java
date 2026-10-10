package com.tamaka.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public record StatusUpdateRequest(
    @NotBlank String status
) {}