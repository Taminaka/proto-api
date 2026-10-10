package com.tamaka.tourism.dto;

import lombok.*;

public record LoginResponse(
    String token,
    String email,
    String fullName,
    java.util.Set<String> roles,
    String activeMode
) {}