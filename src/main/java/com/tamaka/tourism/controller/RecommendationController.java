package com.tamaka.tourism.controller;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.User;
import com.tamaka.tourism.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/attractions")
    @PreAuthorize("hasRole('TURISTA')")
    public ResponseEntity<RecommendationResponse<AttractionRecommendationItem>> recommendAttractions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.time.LocalDate date
    ) {
        RecommendationRequest request = new RecommendationRequest(categoryId, date);
        return ResponseEntity.ok(recommendationService.recommendAttractions(user, request));
    }

    @GetMapping("/services")
    @PreAuthorize("hasRole('TURISTA')")
    public ResponseEntity<RecommendationResponse<ServiceRecommendationItem>> recommendServices(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.time.LocalDate date
    ) {
        RecommendationRequest request = new RecommendationRequest(categoryId, date);
        return ResponseEntity.ok(recommendationService.recommendServices(user, request));
    }

    @GetMapping("/events")
    @PreAuthorize("hasRole('TURISTA')")
    public ResponseEntity<RecommendationResponse<EventRecommendationItem>> recommendEvents(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.time.LocalDate date
    ) {
        RecommendationRequest request = new RecommendationRequest(categoryId, date);
        return ResponseEntity.ok(recommendationService.recommendEvents(user, request));
    }

    @GetMapping("/attractions/{id}/alternatives")
    @PreAuthorize("hasRole('TURISTA')")
    public ResponseEntity<RecommendationResponse<AttractionListResponse>> getAlternatives(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.getAlternativesForHighOccupancy(id));
    }
}