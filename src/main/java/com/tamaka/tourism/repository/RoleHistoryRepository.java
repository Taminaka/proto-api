package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.RoleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleHistoryRepository extends JpaRepository<RoleHistory, Long> {
    List<RoleHistory> findByUserIdOrderByChangedAtDesc(Long userId);
}