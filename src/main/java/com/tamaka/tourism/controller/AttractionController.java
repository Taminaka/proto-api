package com.tamaka.tourism.controller;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.User;
import com.tamaka.tourism.service.AttractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attractions")
@RequiredArgsConstructor
public class AttractionController {
    private final AttractionService attractionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<AttractionResponse> create(@AuthenticationPrincipal User user, @Valid @RequestBody AttractionRequest request) {
        return ResponseEntity.ok(attractionService.create(user, request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttractionResponse>> listAllForAdmin() {
        return ResponseEntity.ok(attractionService.listAllForAdmin());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<AttractionResponse> update(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody AttractionRequest request) {
        return ResponseEntity.ok(attractionService.update(id, user, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody AttractionStatusUpdateRequest request) {
        attractionService.updateStatus(id, user, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/occupancy")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<Void> updateOccupancy(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody OccupancyUpdateRequest request) {
        attractionService.updateOccupancy(id, user, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, @AuthenticationPrincipal User user) {
        attractionService.deactivate(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<Void> reactivate(@PathVariable Long id, @AuthenticationPrincipal User user) {
        attractionService.reactivate(id, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public")
    public ResponseEntity<List<AttractionListResponse>> listPublic(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(attractionService.listActivePublic(name, categoryId));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<AttractionResponse> getPublic(@PathVariable Long id) {
        return ResponseEntity.ok(attractionService.getById(id));
    }

    @GetMapping("/public/{id}/alternatives")
    public ResponseEntity<RecommendationResponse<AttractionListResponse>> getAlternatives(@PathVariable Long id) {
        return ResponseEntity.ok(attractionService.findAlternatives(id,
            attractionService.getById(id).categoryIds().stream().findFirst().orElseThrow()));
    }
}