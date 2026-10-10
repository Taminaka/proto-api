package com.tamaka.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "commerces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commerce {
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

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "website")
    private String website;

    @Column(name = "images", columnDefinition = "TEXT")
    @Builder.Default
    private String images = "";

    @Column(name = "schedules", columnDefinition = "TEXT")
    @Builder.Default
    private String schedules = "[]";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
        name = "commerce_categories",
        joinColumns = @JoinColumn(name = "commerce_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "commerce", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Service> services = new HashSet<>();

    @OneToMany(mappedBy = "commerce", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Event> events = new HashSet<>();

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