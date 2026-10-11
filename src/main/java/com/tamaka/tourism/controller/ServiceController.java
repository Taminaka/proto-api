package com.tamaka.tourism.controller;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.User;
import com.tamaka.tourism.service.ServiceManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceManager serviceManager;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<ServiceResponse> getOwn(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(serviceManager.getOwnById(id, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<ServiceResponse> update(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceManager.update(id, user, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        serviceManager.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> toggleVisibility(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody VisibilityUpdateRequest request) {
        serviceManager.toggleVisibility(id, user, request);
        return ResponseEntity.ok().build();
    }

    // Public endpoints
    @GetMapping("/public")
    public ResponseEntity<List<ServiceListResponse>> listPublic(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long commerceId
    ) {
        return ResponseEntity.ok(serviceManager.listActivePublic(name, categoryId, commerceId));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ServiceResponse> getPublic(@PathVariable Long id) {
        return ResponseEntity.ok(serviceManager.getById(id));
    }

    // Admin endpoints
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminUpdateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        serviceManager.adminUpdateStatus(id, request);
        return ResponseEntity.ok().build();
    }
}