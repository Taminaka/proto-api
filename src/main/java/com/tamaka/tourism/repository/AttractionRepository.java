package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.Attraction;
import com.tamaka.tourism.entity.EntityStatus;
import com.tamaka.tourism.entity.Category;
import com.tamaka.tourism.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    List<Attraction> findByStatus(EntityStatus status);

    List<Attraction> findByCategoriesContaining(Category category);

    @Query("SELECT a FROM Attraction a WHERE a.status = com.tamaka.tourism.entity.EntityStatus.ACTIVE " +
           "AND (:name IS NULL OR a.name LIKE CONCAT('%', :name, '%')) " +
           "AND (:categoryId IS NULL OR a.id IN (SELECT sub.id FROM Attraction sub JOIN sub.categories subcat WHERE subcat.id = :categoryId))")
    List<Attraction> findActiveFiltered(@Param("name") String name, @Param("categoryId") Long categoryId);

    List<Attraction> findByService(Service service);

    @Query("SELECT a FROM Attraction a WHERE :category MEMBER OF a.categories " +
           "AND (CASE WHEN a.capacity IS NULL OR a.capacity = 0 THEN 'LOW' " +
           "WHEN (CAST(a.currentOccupancy AS DOUBLE) / a.capacity * 100) >= 80 THEN 'HIGH' " +
           "WHEN (CAST(a.currentOccupancy AS DOUBLE) / a.capacity * 100) >= 50 THEN 'MEDIUM' " +
           "ELSE 'LOW' END) != :level")
    List<Attraction> findByCategoryAndOccupancyLevelNot(@Param("category") Category category, @Param("level") String level);

    @Query("SELECT a FROM Attraction a WHERE a.status = com.tamaka.tourism.entity.EntityStatus.ACTIVE " +
           "AND a.currentOccupancy < a.capacity * 0.8")
    List<Attraction> findWithAvailableCapacity();
}