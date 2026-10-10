package com.tamaka.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public record ResetPasswordRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 8) String newPassword
) {}