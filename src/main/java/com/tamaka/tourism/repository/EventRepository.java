package com.tamaka.tourism.repository;

import com.tamaka.tourism.entity.Event;
import com.tamaka.tourism.entity.EntityStatus;
import com.tamaka.tourism.entity.EventType;
import com.tamaka.tourism.entity.Commerce;
import com.tamaka.tourism.entity.Attraction;
import com.tamaka.tourism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EntityStatus status);

    List<Event> findByEventType(EventType eventType);

    List<Event> findByAttraction(Attraction attraction);

    List<Event> findByCommerce(Commerce commerce);
    long countByCreatedBy(User user);
    List<Event> findByCreatedBy(User user);

    @Query("SELECT e FROM Event e WHERE e.status = com.tamaka.tourism.entity.EntityStatus.ACTIVE " +
           "AND e.endDate > :now " +
           "AND e.eventType = COALESCE(:type, e.eventType) " +
           "AND e.startDate >= COALESCE(:startDate, e.startDate) " +
           "AND e.endDate <= COALESCE(:endDate, e.endDate) " +
           "AND e.location LIKE CONCAT('%', COALESCE(:location, ''), '%')")
    List<Event> findActiveFiltered(
            @Param("now") LocalDateTime now,
            @Param("type") EventType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("location") String location
    );
}