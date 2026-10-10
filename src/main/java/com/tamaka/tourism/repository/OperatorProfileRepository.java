package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.OperatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorProfileRepository extends JpaRepository<OperatorProfile, Long> {
    Optional<OperatorProfile> findByUserId(Long userId);

    Optional<OperatorProfile> findByBusinessNit(String businessNit);
}