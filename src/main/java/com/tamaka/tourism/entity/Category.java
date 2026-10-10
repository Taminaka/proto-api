package com.tamaka.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<Commerce> commerces = new HashSet<>();

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<Service> services = new HashSet<>();

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<Attraction> attractions = new HashSet<>();
}