package com.tamaka.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public record EnableOperatorModeRequest(
    @NotBlank String businessName,
    @NotBlank String businessNit,
    String businessAddress,
    String businessPhone
) {}