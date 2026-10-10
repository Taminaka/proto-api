package com.tamaka.tourism.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public record ForgotPasswordRequest(
    @Email @NotBlank String email
) {}