package com.tamaka.tourism.service;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.*;
import com.tamaka.tourism.exception.BusinessException;
import com.tamaka.tourism.exception.ResourceNotFoundException;
import com.tamaka.tourism.repository.*;
import com.tamaka.tourism.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final OperatorProfileRepository operatorProfileRepository;
    private final RoleHistoryRepository roleHistoryRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AttractionService attractionService;
    private final StorageService storageService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered");
        }
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .roles(java.util.Set.of(Role.TURISTA))
                .activeMode(ActiveMode.TURISTA)
                .status(UserStatus.ACTIVE)
                .isVerified(false)
                .build();
        userRepository.save(user);
        createToken(user, TokenType.VERIFICATION);
    }

    @Transactional
    public void verify(VerifyRequest request) {
        Token token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException("Invalid or expired token"));
        if (token.getTokenType() != TokenType.VERIFICATION) {
            throw new BusinessException("Invalid token type");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token expired");
        }
        User user = token.getUser();
        user.setIsVerified(true);
        userRepository.save(user);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials");
        }
        if (!user.isEnabled()) {
            throw new BusinessException("Account not verified or suspended");
        }
        var roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        String token = jwtUtil.generateToken(user.getEmail(), roles, user.getActiveMode().name());
        return new LoginResponse(token, user.getEmail(), user.getFullName(), roles, user.getActiveMode().name());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Email not found"));
        createToken(user, TokenType.PASSWORD_RESET);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Token token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException("Invalid or expired token"));
        if (token.getTokenType() != TokenType.PASSWORD_RESET) {
            throw new BusinessException("Invalid token type");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token expired");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }

    public UserResponse getMyInfo(User user) {
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.address() != null) user.setAddress(request.address());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String uploadAvatar(User user, MultipartFile file) {
        return storageService.replaceAvatar(user, file).url();
    }

    @Transactional
    public void deleteAccount(User user) {
        user.setStatus(UserStatus.INACTIVE);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void enableOperatorMode(User user, EnableOperatorModeRequest request) {
        if (user.getRoles().contains(Role.OPERADOR)) {
            throw new BusinessException("Already an operator");
        }
        if (operatorProfileRepository.findByBusinessNit(request.businessNit()).isPresent()) {
            throw new BusinessException("Business NIT already registered");
        }
        OperatorProfile profile = OperatorProfile.builder()
                .user(user)
                .businessName(request.businessName())
                .businessNit(request.businessNit())
                .businessAddress(request.businessAddress())
                .businessPhone(request.businessPhone())
                .isApproved(false)
                .build();
        operatorProfileRepository.save(profile);
        user.getRoles().add(Role.OPERADOR);
        user.setActiveMode(ActiveMode.OPERADOR);
        userRepository.save(user);
        logRoleChange(user, Role.OPERADOR, "ADDED", user);
    }

    @Transactional
    public void registerAccreditation(User user, RegisterAccreditationRequest request) {
        OperatorProfile profile = operatorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Operator profile not found"));
        profile.setAccreditationText(request.accreditationText());
        profile.setAccreditationDocumentUrl(request.accreditationDocumentUrl());
        operatorProfileRepository.save(profile);
    }

    @Transactional
    public void switchMode(User user, SwitchModeRequest request) {
        ActiveMode mode = ActiveMode.valueOf(request.mode().toUpperCase());
        if (mode == ActiveMode.OPERADOR && !user.getRoles().contains(Role.OPERADOR)) {
            throw new BusinessException("Operator mode not enabled");
        }
        user.setActiveMode(mode);
        userRepository.save(user);
    }

    // Admin methods
    public java.util.List<AccountListResponse> listAccounts(String email, String status, String role) {
        return userRepository.findAll().stream()
                .filter(u -> email == null || u.getEmail().toLowerCase().contains(email.toLowerCase()))
                .filter(u -> status == null || u.getStatus().name().equalsIgnoreCase(status))
                .filter(u -> role == null || u.getRoles().stream().anyMatch(r -> r.name().equalsIgnoreCase(role)))
                .map(this::mapToAccountListResponse)
                .toList();
    }

    public UserResponse getAccountDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserResponse(user);
    }

    @Transactional
    public void assignGestorRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRoles().add(Role.GESTOR)) {
            userRepository.save(user);
            logRoleChange(user, Role.GESTOR, "ADDED", null);
        }
    }

    @Transactional
    public void removeGestorRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRoles().remove(Role.GESTOR)) {
            userRepository.save(user);
            logRoleChange(user, Role.GESTOR, "REMOVED", null);
        }
    }

    @Transactional
    public void suspendAccount(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    @Transactional
    public void reactivateAccount(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    public java.util.List<RoleHistoryResponse> getRoleHistory(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return roleHistoryRepository.findByUserIdOrderByChangedAtDesc(id).stream()
                .map(rh -> new RoleHistoryResponse(
                        rh.getId(), rh.getRole().name(), rh.getAction(),
                        rh.getChangedBy() != null ? rh.getChangedBy().getEmail() : "SYSTEM",
                        rh.getChangedAt()
                )).toList();
    }

    @Transactional
    public void createAdminIfNotExists(String email, String password, String fullName) {
        if (!userRepository.existsByEmail(email)) {
            User admin = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .roles(java.util.Set.of(Role.ADMIN, Role.TURISTA))
                    .activeMode(ActiveMode.TURISTA)
                    .status(UserStatus.ACTIVE)
                    .isVerified(true)
                    .build();
            userRepository.save(admin);
        }
    }

    @Transactional
    public AttractionResponse createAttractionAsGestor(User gestor, AttractionRequest request) {
        return attractionService.create(gestor, request);
    }

    private void createToken(User user, TokenType type) {
        String tokenValue = UUID.randomUUID().toString();
        Token token = Token.builder()
                .token(tokenValue)
                .tokenType(type)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();
        tokenRepository.save(token);
        System.out.println("=== " + type.name() + " TOKEN for " + user.getEmail() + ": " + tokenValue + " ===");
    }

    private void logRoleChange(User user, Role role, String action, User changedBy) {
        RoleHistory history = RoleHistory.builder()
                .user(user)
                .role(role)
                .action(action)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
        roleHistoryRepository.save(history);
    }

    private UserResponse mapToUserResponse(User user) {
        OperatorProfileResponse opResponse = null;
        if (user.getOperatorProfile() != null) {
            OperatorProfile op = user.getOperatorProfile();
            opResponse = new OperatorProfileResponse(
                    op.getId(), op.getBusinessName(), op.getBusinessNit(),
                    op.getBusinessAddress(), op.getBusinessPhone(),
                    op.getAccreditationText(), op.getAccreditationDocumentUrl(),
                    op.getIsApproved()
            );
        }
        return new UserResponse(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getPhone(), user.getAddress(), user.getAvatarUrl(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getActiveMode().name(), user.getStatus().name(),
                user.getIsVerified(), opResponse
        );
    }

    private AccountListResponse mapToAccountListResponse(User user) {
        return new AccountListResponse(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getActiveMode().name(), user.getStatus().name(),
                user.getIsVerified(), user.getCreatedAt()
        );
    }
}