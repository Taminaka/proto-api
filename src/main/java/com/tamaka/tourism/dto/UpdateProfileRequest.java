package com.tamaka.tourism.dto;

import lombok.*;

public record UpdateProfileRequest(
    String fullName,
    String phone,
    String address,
    String avatarUrl
) {}