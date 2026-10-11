package com.tamaka.tourism.controller;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.User;
import com.tamaka.tourism.service.CommerceService;
import com.tamaka.tourism.service.ServiceManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commerces")
@RequiredArgsConstructor
public class CommerceController {
    private final CommerceService commerceService;
    private final ServiceManager serviceManager;

    // Operator endpoints
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<CommerceResponse> create(@AuthenticationPrincipal User user, @Valid @RequestBody CommerceRequest request) {
        return ResponseEntity.ok(commerceService.create(user, request));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<List<CommerceListResponse>> listOwn(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(commerceService.listOwn(user));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommerceListResponse>> listAllForAdmin() {
        return ResponseEntity.ok(commerceService.listAllForAdmin());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<CommerceResponse> getOwn(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(commerceService.getOwnById(id, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<CommerceResponse> update(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody CommerceRequest request) {
        return ResponseEntity.ok(commerceService.update(id, user, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody StatusUpdateRequest request) {
        commerceService.updateStatus(id, user, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, @AuthenticationPrincipal User user) {
        commerceService.deactivateOwn(user, id);
        return ResponseEntity.ok().build();
    }

    // Service endpoints under commerce
    @PostMapping("/{commerceId}/services")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<ServiceResponse> createService(@PathVariable Long commerceId, @AuthenticationPrincipal User user, @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceManager.create(commerceId, user, request));
    }

    @GetMapping("/{commerceId}/services")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<List<ServiceListResponse>> listServices(@PathVariable Long commerceId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(serviceManager.listByCommerce(commerceId, user));
    }

    // Public endpoints
    @GetMapping("/public")
    public ResponseEntity<List<CommerceListResponse>> listPublic(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(commerceService.listActivePublic(name, categoryId));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<CommerceResponse> getPublic(@PathVariable Long id) {
        return ResponseEntity.ok(commerceService.getById(id));
    }

    @GetMapping("/public/{commerceId}/services")
    public ResponseEntity<List<ServiceListResponse>> listServicesPublic(
            @PathVariable Long commerceId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(serviceManager.listActivePublic(name, categoryId, commerceId));
    }

    // Admin endpoints
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminUpdateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        commerceService.adminUpdateStatus(id, request);
        return ResponseEntity.ok().build();
    }
}