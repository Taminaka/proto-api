package com.tamaka.tourism.controller;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.*;
import com.tamaka.tourism.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping("/destination")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<EventResponse> createDestinationEvent(@AuthenticationPrincipal User user, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.createDestinationEvent(user, request));
    }

    @PostMapping("/commerce")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<EventResponse> createCommerceEvent(@AuthenticationPrincipal User user, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.createCommerceEvent(user, request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> listAllForAdmin() {
        return ResponseEntity.ok(eventService.listAllForAdmin());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('GESTOR', 'OPERADOR', 'ADMIN')")
    public ResponseEntity<List<EventResponse>> listMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.listOwn(user));
    }

    @PutMapping("/destination/{id}")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<EventResponse> updateDestinationEvent(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateDestinationEvent(id, user, request));
    }

    @PutMapping("/commerce/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<EventResponse> updateCommerceEvent(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateCommerceEvent(id, user, request));
    }

    @PutMapping("/destination/{id}/deactivate")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    public ResponseEntity<Void> deactivateDestinationEvent(@PathVariable Long id, @AuthenticationPrincipal User user) {
        eventService.deactivateDestinationEvent(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/commerce/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> deactivateCommerceEvent(@PathVariable Long id, @AuthenticationPrincipal User user) {
        eventService.deactivateCommerceEvent(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminUpdateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        eventService.adminUpdateStatus(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public")
    public ResponseEntity<List<EventListResponse>> listPublic(
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String location
    ) {
        return ResponseEntity.ok(eventService.listActivePublic(type, startDate, endDate, location));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<EventResponse> getPublic(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }
}