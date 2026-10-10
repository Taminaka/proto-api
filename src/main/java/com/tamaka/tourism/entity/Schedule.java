package com.tamaka.tourism.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    private String openTime; // HH:mm format

    private String closeTime; // HH:mm format

    private Boolean closed;

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}