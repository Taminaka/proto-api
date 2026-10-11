package com.tamaka.tourism.service;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.*;
import com.tamaka.tourism.exception.BusinessException;
import com.tamaka.tourism.exception.ResourceNotFoundException;
import com.tamaka.tourism.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceManager {
    private final ServiceRepository serviceRepository;
    private final CommerceRepository commerceRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ServiceResponse create(Long commerceId, User owner, ServiceRequest request) {
        Commerce commerce = (owner.getRoles().contains(Role.ADMIN) ? commerceRepository.findById(commerceId) : commerceRepository.findByIdAndOwner(commerceId, owner))
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found or access denied"));
        Set<Category> categories = getCategories(request.categoryIds());
        com.tamaka.tourism.entity.Service svc = com.tamaka.tourism.entity.Service.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .durationMinutes(request.durationMinutes())
                .images(request.images() != null ? String.join(",", request.images()) : "")
                .status(EntityStatus.ACTIVE)
                .isVisible(request.isVisible() != null ? request.isVisible() : true)
                .commerce(commerce)
                .categories(categories)
                .build();
        return mapToResponse(serviceRepository.save(svc));
    }

    public java.util.List<ServiceListResponse> listByCommerce(Long commerceId, User owner) {
        Commerce commerce = (owner.getRoles().contains(Role.ADMIN) ? commerceRepository.findById(commerceId) : commerceRepository.findByIdAndOwner(commerceId, owner))
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found or access denied"));
        return serviceRepository.findByCommerce(commerce).stream().map(this::mapToListResponse).toList();
    }

    public java.util.List<ServiceListResponse> listActivePublic(String name, Long categoryId, Long commerceId) {
        return serviceRepository.findActiveFiltered(name == null ? "" : name, categoryId, commerceId).stream().map(this::mapToListResponse).toList();
    }

    public ServiceResponse getById(Long id) {
        return serviceRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
    }

    public ServiceResponse getOwnById(Long id, User owner) {
        return serviceRepository.findById(id).map(s -> {
            if (!owner.getRoles().contains(Role.ADMIN) && !s.getCommerce().getOwner().getId().equals(owner.getId())) {
                throw new BusinessException("Access denied");
            }
            return mapToResponse(s);
        }).orElseThrow(() -> new ResourceNotFoundException("Service not found"));
    }

    @Transactional
    public ServiceResponse update(Long id, User owner, ServiceRequest request) {
        com.tamaka.tourism.entity.Service svc = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        if (!owner.getRoles().contains(Role.ADMIN) && !svc.getCommerce().getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("Access denied");
        }
        svc.setName(request.name());
        svc.setDescription(request.description());
        svc.setPrice(request.price());
        svc.setDurationMinutes(request.durationMinutes());
        if (request.images() != null) {
            svc.setImages(String.join(",", request.images()));
        }
        if (request.categoryIds() != null) {
            svc.getCategories().clear();
            svc.getCategories().addAll(getCategories(request.categoryIds()));
        }
        if (request.isVisible() != null) {
            svc.setIsVisible(request.isVisible());
        }
        return mapToResponse(serviceRepository.save(svc));
    }

    @Transactional
    public void delete(Long id, User owner) {
        com.tamaka.tourism.entity.Service svc = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        if (!owner.getRoles().contains(Role.ADMIN) && !svc.getCommerce().getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("Access denied");
        }
        serviceRepository.delete(svc);
    }

    @Transactional
    public void toggleVisibility(Long id, User owner, VisibilityUpdateRequest request) {
        com.tamaka.tourism.entity.Service svc = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        if (!owner.getRoles().contains(Role.ADMIN) && !svc.getCommerce().getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("Access denied");
        }
        svc.setIsVisible(request.visible());
        serviceRepository.save(svc);
    }

    @Transactional
    public void adminUpdateStatus(Long id, StatusUpdateRequest request) {
        com.tamaka.tourism.entity.Service svc = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        svc.setStatus(EntityStatus.valueOf(request.status().toUpperCase()));
        serviceRepository.save(svc);
    }

    private Set<Category> getCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return Set.of();
        return categoryIds.stream()
                .map(cid -> categoryRepository.findById(cid)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + cid)))
                .collect(Collectors.toSet());
    }

    private ServiceResponse mapToResponse(com.tamaka.tourism.entity.Service s) {
        return new ServiceResponse(
                s.getId(), s.getName(), s.getDescription(), s.getPrice(), s.getDurationMinutes(),
                s.getImages() != null && !s.getImages().isEmpty() ? java.util.Arrays.asList(s.getImages().split(",")) : java.util.List.of(), s.getStatus().name(), s.getIsVisible(),
                s.getCommerce().getId(), s.getCommerce().getName(),
                s.getCategories().stream().map(Category::getId).collect(Collectors.toSet()),
                s.getCategories().stream().map(Category::getName).collect(Collectors.toSet()),
                s.getCreatedAt(), s.getUpdatedAt()
        );
    }

    private ServiceListResponse mapToListResponse(com.tamaka.tourism.entity.Service s) {
        return new ServiceListResponse(
                s.getId(), s.getName(), s.getDescription(), s.getPrice(), s.getDurationMinutes(),
                s.getImages() != null && !s.getImages().isEmpty() ? java.util.Arrays.asList(s.getImages().split(",")) : java.util.List.of(), s.getIsVisible(),
                s.getCommerce().getId(), s.getCommerce().getName(),
                s.getCategories().stream().map(Category::getName).collect(Collectors.toSet())
        );
    }
}