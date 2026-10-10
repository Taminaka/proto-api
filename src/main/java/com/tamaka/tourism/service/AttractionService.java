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
public class AttractionService {
    private final AttractionRepository attractionRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public AttractionResponse create(User gestor, AttractionRequest request) {
        Set<Category> categories = getCategories(request.categoryIds());
        com.tamaka.tourism.entity.Service svc = null;
        if (request.serviceId() != null) {
            svc = serviceRepository.findById(request.serviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        }
        Attraction attraction = Attraction.builder()
                .name(request.name())
                .description(request.description())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .images(request.images() != null ? String.join(",", request.images()) : "")
                .status(EntityStatus.ACTIVE)
                .accessInfo(request.accessInfo())
                .restrictions(request.restrictions())
                .capacity(request.capacity())
                .currentOccupancy(0)
                .categories(categories)
                .service(svc)
                .createdBy(gestor)
                .build();
        return mapToResponse(attractionRepository.save(attraction));
    }

    public java.util.List<AttractionListResponse> listActivePublic(String name, Long categoryId) {
        return attractionRepository.findActiveFiltered(name == null ? "" : name, categoryId).stream().map(this::mapToListResponse).toList();
    }

    public java.util.List<AttractionResponse> listAllForAdmin() {
        return attractionRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public AttractionResponse getById(Long id) {
        return attractionRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found"));
    }

    @Transactional
    public AttractionResponse update(Long id, User gestor, AttractionRequest request) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found"));
        attraction.setName(request.name());
        attraction.setDescription(request.description());
        attraction.setAddress(request.address());
        attraction.setLatitude(request.latitude());
        attraction.setLongitude(request.longitude());
        if (request.images() != null) {
            attraction.setImages(String.join(",", request.images()));
        }
        attraction.setAccessInfo(request.accessInfo());
        attraction.setRestrictions(request.restrictions());
        if (request.capacity() != null) attraction.setCapacity(request.capacity());
        if (request.categoryIds() != null) {
            attraction.getCategories().clear();
            attraction.getCategories().addAll(getCategories(request.categoryIds()));
        }
        if (request.serviceId() != null) {
            com.tamaka.tourism.entity.Service svc = serviceRepository.findById(request.serviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
            attraction.setService(svc);
        } else {
            attraction.setService(null);
        }
        return mapToResponse(attractionRepository.save(attraction));
    }

    @Transactional
    public void updateStatus(Long id, User gestor, AttractionStatusUpdateRequest request) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found"));
        attraction.setStatus(EntityStatus.valueOf(request.status().toUpperCase()));
        attractionRepository.save(attraction);
    }

    @Transactional
    public void updateOccupancy(Long id, User gestor, OccupancyUpdateRequest request) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found"));
        if (attraction.getCapacity() != null && request.currentOccupancy() > attraction.getCapacity()) {
            throw new BusinessException("Occupancy cannot exceed capacity");
        }
        attraction.setCurrentOccupancy(request.currentOccupancy());
        attractionRepository.save(attraction);
    }

    @Transactional
    public void deactivate(Long id, User gestor) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found"));
        attraction.setStatus(EntityStatus.INACTIVE);
        attractionRepository.save(attraction);
    }

    @Transactional
    public void reactivate(Long id, User gestor) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found"));
        attraction.setStatus(EntityStatus.ACTIVE);
        attractionRepository.save(attraction);
    }

    public RecommendationResponse<AttractionListResponse> findAlternatives(Long attractionId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Attraction original = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found"));
        List<RecommendationItem<AttractionListResponse>> items = attractionRepository.findByCategoryAndOccupancyLevelNot(category, original.getOccupancyLevel().name()).stream()
                .filter(a -> !a.getId().equals(attractionId))
                .map(this::mapToListResponse)
                .map(a -> new RecommendationItem<>(a, "Same category '" + category.getName() + "' with " + a.occupancyLevel() + " occupancy", 1.0))
                .toList();
        return new RecommendationResponse<>(items);
    }

    private Set<Category> getCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return Set.of();
        return categoryIds.stream()
                .map(cid -> categoryRepository.findById(cid)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + cid)))
                .collect(Collectors.toSet());
    }

    private AttractionResponse mapToResponse(Attraction a) {
        return new AttractionResponse(
                a.getId(), a.getName(), a.getDescription(), a.getAddress(),
                a.getLatitude(), a.getLongitude(), a.getImages() != null && !a.getImages().isEmpty() ? java.util.Arrays.asList(a.getImages().split(",")) : java.util.List.of(),
                a.getStatus().name(), a.getAccessInfo(), a.getRestrictions(),
                a.getCapacity(), a.getCurrentOccupancy(), a.getOccupancyLevel().name(),
                a.getCategories().stream().map(Category::getId).collect(Collectors.toSet()),
                a.getCategories().stream().map(Category::getName).collect(Collectors.toSet()),
                a.getService() != null ? a.getService().getId() : null,
                a.getService() != null ? a.getService().getName() : null,
                a.getCreatedBy() != null ? a.getCreatedBy().getId() : null,
                a.getCreatedBy() != null ? a.getCreatedBy().getFullName() : null,
                a.getCreatedAt(), a.getUpdatedAt()
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
}