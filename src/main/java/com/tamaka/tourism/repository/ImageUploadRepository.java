package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.ImageUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ImageUploadRepository extends JpaRepository<ImageUpload, Long> {
    long countByUserIdAndPurposeAndCreatedAtAfter(Long userId, String purpose, LocalDateTime after);

    @Query("""
            select i from ImageUpload i
            where i.user.id = :userId and i.purpose = :purpose
            order by i.createdAt desc
            """)
    java.util.List<ImageUpload> findRecentByUserAndPurpose(
            @Param("userId") Long userId,
            @Param("purpose") String purpose,
            org.springframework.data.domain.Pageable pageable
    );
}
