package com.tamaka.tourism.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public record CategoryRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description
) {}