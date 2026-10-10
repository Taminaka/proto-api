package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.Service;
import com.tamaka.tourism.entity.EntityStatus;
import com.tamaka.tourism.entity.Commerce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByCommerce(Commerce commerce);

    List<Service> findByCommerceAndStatus(Commerce commerce, EntityStatus status);

    @Query("SELECT s FROM Service s WHERE s.status = com.tamaka.tourism.entity.EntityStatus.ACTIVE " +
           "AND s.isVisible = true " +
           "AND (:name IS NULL OR s.name LIKE CONCAT('%', :name, '%')) " +
           "AND (:categoryId IS NULL OR s.id IN (SELECT sub.id FROM Service sub JOIN sub.categories subcat WHERE subcat.id = :categoryId)) " +
           "AND (:commerceId IS NULL OR s.commerce.id = :commerceId)")
    List<Service> findActiveFiltered(@Param("name") String name, @Param("categoryId") Long categoryId, @Param("commerceId") Long commerceId);

    Optional<Service> findByIdAndCommerce(Long id, Commerce commerce);
}