package com.tamaka.tourism.service;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.*;
import com.tamaka.tourism.exception.BusinessException;
import com.tamaka.tourism.exception.ResourceNotFoundException;
import com.tamaka.tourism.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.findByName(request.name()).isPresent()) {
            throw new BusinessException("Category already exists");
        }
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .status(EntityStatus.ACTIVE)
                .build();
        return mapToResponse(categoryRepository.save(category));
    }

    public java.util.List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public java.util.List<CategoryResponse> listActive() {
        return categoryRepository.findByStatus(EntityStatus.ACTIVE).stream().map(this::mapToResponse).toList();
    }

    public CategoryResponse getById(Long id) {
        return categoryRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getName().equals(request.name()) && categoryRepository.findByName(request.name()).isPresent()) {
            throw new BusinessException("Category name already exists");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public void updateStatus(Long id, StatusUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setStatus(EntityStatus.valueOf(request.status().toUpperCase()));
        categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getCommerces().isEmpty() || !category.getServices().isEmpty() || !category.getAttractions().isEmpty()) {
            throw new BusinessException("Cannot delete category with associated entities");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), category.getStatus().name());
    }
}