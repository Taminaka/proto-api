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
public class CommerceService {
    private final CommerceRepository commerceRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public CommerceResponse create(User owner, CommerceRequest request) {
        Set<Category> categories = getCategories(request.categoryIds());
        Commerce commerce = Commerce.builder()
                .name(request.name())
                .description(request.description())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .phone(request.phone())
                .email(request.email())
                .website(request.website())
                .images(request.images() != null ? String.join(",", request.images()) : "")
                .schedules(request.schedules() != null ? request.schedules().toString() : "[]")
                .status(EntityStatus.ACTIVE)
                .owner(owner)
                .categories(categories)
                .build();
        return mapToResponse(commerceRepository.save(commerce));
    }

    public java.util.List<CommerceListResponse> listOwn(User owner) {
        return commerceRepository.findByOwner(owner).stream().map(this::mapToListResponse).toList();
    }

    public java.util.List<CommerceListResponse> listActivePublic(String name, Long categoryId) {
        return commerceRepository.findActiveFiltered(name == null ? "" : name, categoryId).stream().map(this::mapToListResponse).toList();
    }

    public CommerceResponse getById(Long id) {
        return commerceRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found"));
    }

    public CommerceResponse getOwnById(Long id, User owner) {
        return (owner.getRoles().contains(Role.ADMIN) ? commerceRepository.findById(id) : commerceRepository.findByIdAndOwner(id, owner)).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found or access denied"));
    }

    @Transactional
    public CommerceResponse update(Long id, User owner, CommerceRequest request) {
        Commerce commerce = (owner.getRoles().contains(Role.ADMIN) ? commerceRepository.findById(id) : commerceRepository.findByIdAndOwner(id, owner))
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found or access denied"));
        commerce.setName(request.name());
        commerce.setDescription(request.description());
        commerce.setAddress(request.address());
        commerce.setLatitude(request.latitude());
        commerce.setLongitude(request.longitude());
        commerce.setPhone(request.phone());
        commerce.setEmail(request.email());
        commerce.setWebsite(request.website());
        if (request.images() != null) {
            commerce.setImages(String.join(",", request.images()));
        }
        if (request.schedules() != null) {
            commerce.setSchedules(request.schedules().toString());
        }
        if (request.categoryIds() != null) {
            commerce.getCategories().clear();
            commerce.getCategories().addAll(getCategories(request.categoryIds()));
        }
        return mapToResponse(commerceRepository.save(commerce));
    }

    @Transactional
    public void updateStatus(Long id, User owner, StatusUpdateRequest request) {
        Commerce commerce = (owner.getRoles().contains(Role.ADMIN) ? commerceRepository.findById(id) : commerceRepository.findByIdAndOwner(id, owner))
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found or access denied"));
        commerce.setStatus(EntityStatus.valueOf(request.status().toUpperCase()));
        commerceRepository.save(commerce);
    }

    @Transactional
    public void adminUpdateStatus(Long id, StatusUpdateRequest request) {
        Commerce commerce = commerceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found"));
        commerce.setStatus(EntityStatus.valueOf(request.status().toUpperCase()));
        commerceRepository.save(commerce);
    }

    @Transactional
    public void deactivateOwn(User owner, Long id) {
        Commerce commerce = (owner.getRoles().contains(Role.ADMIN) ? commerceRepository.findById(id) : commerceRepository.findByIdAndOwner(id, owner))
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found or access denied"));
        commerce.setStatus(EntityStatus.INACTIVE);
        commerceRepository.save(commerce);
    }

    private Set<Category> getCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return Set.of();
        return categoryIds.stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id)))
                .collect(Collectors.toSet());
    }

    private Schedule mapSchedule(ScheduleRequest req) {
        Schedule s = new Schedule();
        s.setDay(Schedule.DayOfWeek.valueOf(req.day().name()));
        s.setOpenTime(req.openTime());
        s.setCloseTime(req.closeTime());
        s.setClosed(req.closed());
        return s;
    }

    private ScheduleRequest mapToScheduleResponse(Schedule s) {
        return new ScheduleRequest(ScheduleRequest.DayOfWeek.valueOf(s.getDay().name()), s.getOpenTime(), s.getCloseTime(), s.getClosed());
    }

    private CommerceResponse mapToResponse(Commerce c) {
        return new CommerceResponse(
                c.getId(), c.getName(), c.getDescription(), c.getAddress(),
                c.getLatitude(), c.getLongitude(), c.getPhone(), c.getEmail(), c.getWebsite(),
                c.getImages() != null && !c.getImages().isEmpty() ? java.util.Arrays.asList(c.getImages().split(",")) : java.util.List.of(),
                c.getSchedules() != null ? java.util.List.of() : java.util.List.of(),
                c.getStatus().name(), c.getOwner().getId(), c.getOwner().getFullName(),
                c.getCategories().stream().map(Category::getId).collect(Collectors.toSet()),
                c.getCategories().stream().map(Category::getName).collect(Collectors.toSet()),
                c.getCreatedAt(), c.getUpdatedAt()
        );
    }

    private CommerceListResponse mapToListResponse(Commerce c) {
        return new CommerceListResponse(
                c.getId(), c.getName(), c.getDescription(), c.getAddress(),
                c.getLatitude(), c.getLongitude(), c.getPhone(), c.getImages() != null && !c.getImages().isEmpty() ? java.util.Arrays.asList(c.getImages().split(",")) : java.util.List.of(),
                c.getStatus().name(), c.getOwner().getId(), c.getOwner().getFullName(),
                c.getCategories().stream().map(Category::getName).collect(Collectors.toSet()),
                c.getCreatedAt()
        );
    }
}