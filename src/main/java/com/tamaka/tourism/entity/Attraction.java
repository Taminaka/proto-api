package com.tamaka.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "attractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "images", columnDefinition = "TEXT")
    @Builder.Default
    private String images = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "access_info", columnDefinition = "TEXT")
    private String accessInfo;

    @Column(name = "restrictions", columnDefinition = "TEXT")
    private String restrictions;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "current_occupancy")
    @Builder.Default
    private Integer currentOccupancy = 0;

    @ManyToMany
    @JoinTable(
        name = "attraction_categories",
        joinColumns = @JoinColumn(name = "attraction_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public OccupancyLevel getOccupancyLevel() {
        if (capacity == null || capacity == 0) return OccupancyLevel.LOW;
        double percentage = (double) currentOccupancy / capacity * 100;
        if (percentage >= 80) return OccupancyLevel.HIGH;
        if (percentage >= 50) return OccupancyLevel.MEDIUM;
        return OccupancyLevel.LOW;
    }
}