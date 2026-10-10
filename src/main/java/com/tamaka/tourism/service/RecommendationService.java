package com.tamaka.tourism.service;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.*;
import com.tamaka.tourism.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final AttractionRepository attractionRepository;
    private final ServiceRepository serviceRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public RecommendationResponse<AttractionRecommendationItem> recommendAttractions(User user, RecommendationRequest request) {
        List<Attraction> candidates = attractionRepository.findWithAvailableCapacity();
        
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new com.tamaka.tourism.exception.ResourceNotFoundException("Category not found"));
            candidates = candidates.stream()
                    .filter(a -> a.getCategories().contains(category))
                    .toList();
        }

        List<RecommendationItem<AttractionRecommendationItem>> items = candidates.stream()
                .map(a -> scoreAttraction(a, request))
                .sorted(Comparator.comparingDouble((RecommendationItem<AttractionRecommendationItem> item) -> item.score()).reversed())
                .limit(20)
                .toList();

        return new RecommendationResponse<>(items);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse<ServiceRecommendationItem> recommendServices(User user, RecommendationRequest request) {
        List<com.tamaka.tourism.entity.Service> candidates = serviceRepository.findActiveFiltered("", request.categoryId(), null);
        
        List<RecommendationItem<ServiceRecommendationItem>> items = candidates.stream()
                .map(s -> scoreService(s, request))
                .sorted(Comparator.comparingDouble((RecommendationItem<ServiceRecommendationItem> item) -> item.score()).reversed())
                .limit(20)
                .toList();

        return new RecommendationResponse<>(items);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse<EventRecommendationItem> recommendEvents(User user, RecommendationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = request.date() != null ? request.date().atStartOfDay() : now;
        LocalDateTime endOfDay = request.date() != null ? request.date().atTime(23, 59, 59) : null;
        
        List<Event> candidates = eventRepository.findActiveFiltered(now, null, startOfDay, endOfDay, "");
        
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new com.tamaka.tourism.exception.ResourceNotFoundException("Category not found"));
            candidates = candidates.stream()
                    .filter(e -> (e.getAttraction() != null && e.getAttraction().getCategories().contains(category))
                            || (e.getCommerce() != null && e.getCommerce().getCategories().contains(category)))
                    .toList();
        }

        List<RecommendationItem<EventRecommendationItem>> items = candidates.stream()
                .map(e -> scoreEvent(e, request))
                .sorted(Comparator.comparingDouble((RecommendationItem<EventRecommendationItem> item) -> item.score()).reversed())
                .limit(20)
                .toList();

        return new RecommendationResponse<>(items);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse<AttractionListResponse> getAlternativesForHighOccupancy(Long attractionId) {
        Attraction original = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new com.tamaka.tourism.exception.ResourceNotFoundException("Attraction not found"));
        
        if (original.getOccupancyLevel() != OccupancyLevel.HIGH) {
            throw new com.tamaka.tourism.exception.BusinessException("Attraction does not have HIGH occupancy");
        }

        if (original.getCategories().isEmpty()) {
            return new RecommendationResponse<>(List.of());
        }

        Category primaryCategory = original.getCategories().iterator().next();
        List<RecommendationItem<AttractionListResponse>> alternatives = attractionRepository.findByCategoryAndOccupancyLevelNot(primaryCategory, OccupancyLevel.HIGH.name())
                .stream()
                .filter(a -> !a.getId().equals(attractionId))
                .limit(10)
                .map(this::mapToListResponse)
                .map(a -> new RecommendationItem<>(a, "Same category '" + primaryCategory.getName() + "' with " + a.occupancyLevel() + " occupancy", 1.0))
                .toList();

        return new RecommendationResponse<>(alternatives);
    }

    private RecommendationItem<AttractionRecommendationItem> scoreAttraction(Attraction a, RecommendationRequest request) {
        double score = 0;
        List<String> reasons = new ArrayList<>();

        if (a.getOccupancyLevel() == OccupancyLevel.LOW) {
            score += 30;
            reasons.add("Low occupancy");
        } else if (a.getOccupancyLevel() == OccupancyLevel.MEDIUM) {
            score += 15;
            reasons.add("Medium occupancy");
        }

        if (request.categoryId() != null && a.getCategories().stream().anyMatch(c -> c.getId().equals(request.categoryId()))) {
            Category cat = a.getCategories().stream().filter(c -> c.getId().equals(request.categoryId())).findFirst().orElse(null);
            if (cat != null) {
                score += 25;
                reasons.add("Matches category '" + cat.getName() + "'");
            }
        }

        if (a.getStatus() == EntityStatus.ACTIVE) {
            score += 10;
            reasons.add("Active");
        }

        String reason = reasons.isEmpty() ? "Available" : String.join("; ", reasons);
        return new RecommendationItem<>(mapToRecommendationItem(a), reason, score);
    }

    private RecommendationItem<ServiceRecommendationItem> scoreService(com.tamaka.tourism.entity.Service s, RecommendationRequest request) {
        double score = 0;
        List<String> reasons = new ArrayList<>();

        if (request.categoryId() != null && s.getCategories().stream().anyMatch(c -> c.getId().equals(request.categoryId()))) {
            Category cat = s.getCategories().stream().filter(c -> c.getId().equals(request.categoryId())).findFirst().orElse(null);
            if (cat != null) {
                score += 30;
                reasons.add("Matches category '" + cat.getName() + "'");
            }
        }

        if (s.getIsVisible()) {
            score += 10;
            reasons.add("Visible");
        }

        if (s.getStatus() == EntityStatus.ACTIVE) {
            score += 10;
            reasons.add("Active");
        }

        String reason = reasons.isEmpty() ? "Available" : String.join("; ", reasons);
        return new RecommendationItem<>(mapToServiceRecommendationItem(s), reason, score);
    }

    private RecommendationItem<EventRecommendationItem> scoreEvent(Event e, RecommendationRequest request) {
        double score = 0;
        List<String> reasons = new ArrayList<>();

        if (request.date() != null) {
            LocalDateTime eventStart = e.getStartDate();
            if (eventStart.toLocalDate().equals(request.date())) {
                score += 25;
                reasons.add("Happens on selected date");
            }
        }

        if (request.categoryId() != null) {
            Category cat = categoryRepository.findById(request.categoryId()).orElse(null);
            if (cat != null) {
                boolean matches = (e.getAttraction() != null && e.getAttraction().getCategories().contains(cat))
                        || (e.getCommerce() != null && e.getCommerce().getCategories().contains(cat));
                if (matches) {
                    score += 20;
                    reasons.add("Matches category '" + cat.getName() + "'");
                }
            }
        }

        if (e.getStatus() == EntityStatus.ACTIVE) {
            score += 10;
            reasons.add("Active");
        }

        String reason = reasons.isEmpty() ? "Available" : String.join("; ", reasons);
        return new RecommendationItem<>(mapToEventRecommendationItem(e), reason, score);
    }

    private AttractionRecommendationItem mapToRecommendationItem(Attraction a) {
        return new AttractionRecommendationItem(
                a.getId(), a.getName(), a.getDescription(), a.getAddress(),
                a.getLatitude(), a.getLongitude(), a.getImages() != null && !a.getImages().isEmpty() ? java.util.Arrays.asList(a.getImages().split(",")) : java.util.List.of(),
                a.getStatus().name(), a.getCapacity(), a.getCurrentOccupancy(),
                a.getOccupancyLevel().name(),
                a.getCategories().stream().map(Category::getName).collect(Collectors.toSet())
        );
    }

    private AttractionListResponse mapToListResponse(Attraction a) {
        return new AttractionListResponse(
                a.getId(), a.getName(), a.getDescription(), a.getAddress(),
                a.getLatitude(), a.getLongitude(), a.getImages() != null && !a.getImages().isEmpty() ? java.util.Arrays.asList(a.getImages().split(",")) : java.util.List.of(),
                a.getStatus().name(), a.getCapacity(), a.getCurrentOccupancy(),
                a.getOccupancyLevel().name(),
                a.getCategories().stream().map(Category::getName).collect(Collectors.toSet())
        );
    }

    private ServiceRecommendationItem mapToServiceRecommendationItem(com.tamaka.tourism.entity.Service s) {
        return new ServiceRecommendationItem(
                s.getId(), s.getName(), s.getDescription(), s.getPrice(), s.getDurationMinutes(),
                s.getImages() != null ? java.util.List.of(s.getImages().split(",")) : java.util.List.of(), s.getIsVisible(),
                s.getCommerce().getId(), s.getCommerce().getName(),
                s.getCategories().stream().map(Category::getName).collect(Collectors.toSet())
        );
    }

    private EventRecommendationItem mapToEventRecommendationItem(Event e) {
        return new EventRecommendationItem(
                e.getId(), e.getName(), e.getDescription(), e.getEventType().name(),
                e.getStartDate(), e.getEndDate(), e.getLocation(), e.getImages() != null && !e.getImages().isEmpty() ? java.util.Arrays.asList(e.getImages().split(",")) : java.util.List.of(),
                e.getAttraction() != null ? e.getAttraction().getId() : null,
                e.getAttraction() != null ? e.getAttraction().getName() : null,
                e.getCommerce() != null ? e.getCommerce().getId() : null,
                e.getCommerce() != null ? e.getCommerce().getName() : null
        );
    }
}