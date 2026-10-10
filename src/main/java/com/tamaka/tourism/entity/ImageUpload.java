package com.tamaka.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_uploads", indexes = {
        @Index(name = "idx_image_upload_user_purpose_created", columnList = "user_id,purpose,created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String objectKey;

    @Column(nullable = false, length = 40)
    private String purpose;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
