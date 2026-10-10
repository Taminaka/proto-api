package com.tamaka.tourism.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public record ScheduleRequest(
    @NotNull DayOfWeek day,
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$") String openTime,
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$") String closeTime,
    Boolean closed
) {
    public enum DayOfWeek { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }
}