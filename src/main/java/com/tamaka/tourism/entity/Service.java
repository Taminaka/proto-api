package com.tamaka.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "images", columnDefinition = "TEXT")
    @Builder.Default
    private String images = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "is_visible")
    @Builder.Default
    private Boolean isVisible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commerce_id", nullable = false)
    private Commerce commerce;

    @ManyToMany
    @JoinTable(
        name = "service_categories",
        joinColumns = @JoinColumn(name = "service_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Attraction> attractions = new HashSet<>();

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
}