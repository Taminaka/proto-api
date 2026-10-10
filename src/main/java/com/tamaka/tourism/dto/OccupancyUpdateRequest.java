package com.tamaka.tourism.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

public record OccupancyUpdateRequest(
    @Min(0) Integer currentOccupancy
) {}