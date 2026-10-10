package com.tamaka.tourism.controller;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.User;
import com.tamaka.tourism.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.getMyInfo(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal User user, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(user, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal User user) {
        authService.deleteAccount(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/operator-mode")
    public ResponseEntity<Void> enableOperatorMode(@AuthenticationPrincipal User user, @Valid @RequestBody EnableOperatorModeRequest request) {
        authService.enableOperatorMode(user, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/accreditation")
    public ResponseEntity<Void> registerAccreditation(@AuthenticationPrincipal User user, @Valid @RequestBody RegisterAccreditationRequest request) {
        authService.registerAccreditation(user, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/mode")
    public ResponseEntity<Void> switchMode(@AuthenticationPrincipal User user, @Valid @RequestBody SwitchModeRequest request) {
        authService.switchMode(user, request);
        return ResponseEntity.ok().build();
    }

    // Admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountListResponse>> listAccounts(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role
    ) {
        return ResponseEntity.ok(authService.listAccounts(email, status, role));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getAccountDetail(id));
    }

    @PostMapping("/{id}/gestor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignGestor(@PathVariable Long id) {
        authService.assignGestorRole(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/gestor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeGestor(@PathVariable Long id) {
        authService.removeGestorRole(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspend(@PathVariable Long id) {
        authService.suspendAccount(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        authService.reactivateAccount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/role-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleHistoryResponse>> getRoleHistory(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getRoleHistory(id));
    }
}