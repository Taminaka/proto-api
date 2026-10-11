package com.tamaka.tourism.service;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.*;
import com.tamaka.tourism.exception.BusinessException;
import com.tamaka.tourism.exception.ResourceNotFoundException;
import com.tamaka.tourism.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final AttractionRepository attractionRepository;
    private final CommerceRepository commerceRepository;

    @Transactional
    public EventResponse createDestinationEvent(User gestor, EventRequest request) {
        if (request.eventType() != EventType.DESTINO) {
            throw new BusinessException("Invalid event type for destination event");
        }
        Attraction attraction = null;
        if (request.attractionId() != null) {
            attraction = attractionRepository.findById(request.attractionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Attraction not found"));
        }
        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .eventType(EventType.DESTINO)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .location(request.location())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .images(request.images() != null ? String.join(",", request.images()) : "")
                .status(EntityStatus.ACTIVE)
                .attraction(attraction)
                .createdBy(gestor)
                .build();
        return mapToResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse createCommerceEvent(User operator, EventRequest request) {
        if (request.eventType() != EventType.COMERCIO) {
            throw new BusinessException("Invalid event type for commerce event");
        }
        Commerce commerce = commerceRepository.findById(request.commerceId())
                .orElseThrow(() -> new ResourceNotFoundException("Commerce not found"));
        if (!operator.getRoles().contains(Role.ADMIN) && !commerce.getOwner().getId().equals(operator.getId())) {
            throw new BusinessException("Access denied to this commerce");
        }
        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .eventType(EventType.COMERCIO)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .location(request.location())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .images(request.images() != null ? String.join(",", request.images()) : "")
                .status(EntityStatus.ACTIVE)
                .commerce(commerce)
                .createdBy(operator)
                .build();
        return mapToResponse(eventRepository.save(event));
    }

    public java.util.List<EventListResponse> listActivePublic(EventType type, LocalDateTime startDate, LocalDateTime endDate, String location) {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findActiveFiltered(now, type, startDate, endDate, location == null ? "" : location).stream()
                .map(this::mapToListResponse).toList();
    }

    public java.util.List<EventResponse> listAllForAdmin() {
        return eventRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public EventResponse getById(Long id) {
        return eventRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    @Transactional
    public EventResponse updateDestinationEvent(Long id, User gestor, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getEventType() != EventType.DESTINO) {
            throw new BusinessException("Not a destination event");
        }
        updateEventFields(event, request);
        return mapToResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse updateCommerceEvent(Long id, User operator, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getEventType() != EventType.COMERCIO) {
            throw new BusinessException("Not a commerce event");
        }
        if (!operator.getRoles().contains(Role.ADMIN) && (event.getCommerce() == null || !event.getCommerce().getOwner().getId().equals(operator.getId()))) {
            throw new BusinessException("Access denied");
        }
        updateEventFields(event, request);
        return mapToResponse(eventRepository.save(event));
    }

    @Transactional
    public void deactivateDestinationEvent(Long id, User gestor) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getEventType() != EventType.DESTINO) {
            throw new BusinessException("Not a destination event");
        }
        event.setStatus(EntityStatus.INACTIVE);
        eventRepository.save(event);
    }

    @Transactional
    public void deactivateCommerceEvent(Long id, User operator) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getEventType() != EventType.COMERCIO) {
            throw new BusinessException("Not a commerce event");
        }
        if (!operator.getRoles().contains(Role.ADMIN) && (event.getCommerce() == null || !event.getCommerce().getOwner().getId().equals(operator.getId()))) {
            throw new BusinessException("Access denied");
        }
        event.setStatus(EntityStatus.INACTIVE);
        eventRepository.save(event);
    }

    @Transactional
    public void adminUpdateStatus(Long id, StatusUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.setStatus(EntityStatus.valueOf(request.status().toUpperCase()));
        eventRepository.save(event);
    }

    private void updateEventFields(Event event, EventRequest request) {
        event.setName(request.name());
        event.setDescription(request.description());
        event.setStartDate(request.startDate());
        event.setEndDate(request.endDate());
        event.setLocation(request.location());
        event.setLatitude(request.latitude());
        event.setLongitude(request.longitude());
        if (request.images() != null) {
            event.setImages(String.join(",", request.images()));
        }
    }

    private EventResponse mapToResponse(Event e) {
        return new EventResponse(
                e.getId(), e.getName(), e.getDescription(), e.getEventType().name(),
                e.getStartDate(), e.getEndDate(), e.getLocation(), e.getLatitude(), e.getLongitude(),
                e.getImages() != null && !e.getImages().isEmpty() ? java.util.Arrays.asList(e.getImages().split(",")) : java.util.List.of(), e.getStatus().name(),
                e.getAttraction() != null ? e.getAttraction().getId() : null,
                e.getAttraction() != null ? e.getAttraction().getName() : null,
                e.getCommerce() != null ? e.getCommerce().getId() : null,
                e.getCommerce() != null ? e.getCommerce().getName() : null,
                e.getCreatedBy() != null ? e.getCreatedBy().getId() : null,
                e.getCreatedBy() != null ? e.getCreatedBy().getFullName() : null,
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private EventListResponse mapToListResponse(Event e) {
        return new EventListResponse(
                e.getId(), e.getName(), e.getDescription(), e.getEventType().name(),
                e.getStartDate(), e.getEndDate(), e.getLocation(), e.getImages() != null && !e.getImages().isEmpty() ? java.util.Arrays.asList(e.getImages().split(",")) : java.util.List.of(),
                e.getAttraction() != null ? e.getAttraction().getId() : null,
                e.getAttraction() != null ? e.getAttraction().getName() : null,
                e.getCommerce() != null ? e.getCommerce().getId() : null,
                e.getCommerce() != null ? e.getCommerce().getName() : null
        );
    }
}