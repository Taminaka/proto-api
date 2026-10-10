package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.Commerce;
import com.tamaka.tourism.entity.EntityStatus;
import com.tamaka.tourism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommerceRepository extends JpaRepository<Commerce, Long> {
    List<Commerce> findByOwner(User owner);

    List<Commerce> findByStatus(EntityStatus status);

    List<Commerce> findByOwnerAndStatus(User owner, EntityStatus status);

    @Query("SELECT c FROM Commerce c WHERE c.status = com.tamaka.tourism.entity.EntityStatus.ACTIVE " +
           "AND (:name IS NULL OR c.name LIKE CONCAT('%', :name, '%')) " +
           "AND (:categoryId IS NULL OR c.id IN (SELECT sub.id FROM Commerce sub JOIN sub.categories subcat WHERE subcat.id = :categoryId))")
    List<Commerce> findActiveFiltered(@Param("name") String name, @Param("categoryId") Long categoryId);

    Optional<Commerce> findByIdAndOwner(Long id, User owner);
}