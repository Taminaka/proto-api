package com.tamaka.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "action", nullable = false)
    private String action; // ADDED, REMOVED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private java.time.LocalDateTime changedAt;
}