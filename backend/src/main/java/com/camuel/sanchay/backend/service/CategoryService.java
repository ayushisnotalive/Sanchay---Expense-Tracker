package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.CategoryDto;
import com.camuel.sanchay.backend.entity.Category;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.CategoryRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @Cacheable(value = "userCategories", key = "#userId")
    public List<CategoryDto> getCategoriesForUser(Long userId) {
        User user = getUser(userId);
        return categoryRepository.findAllAvailableForUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "userCategories", key = "#userId")
    public CategoryDto createCustomCategory(Long userId, CategoryDto dto) {
        User user = getUser(userId);
        Category category = new Category();
        category.setUser(user);
        category.setName(dto.getName());
        category.setIconName(dto.getIconName());
        category.setCustom(true);

        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    @Transactional
    @CacheEvict(value = "userCategories", key = "#userId")
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser() != null && c.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or cannot be deleted"));

        categoryRepository.delete(category);
    }

    public CategoryDto mapToDto(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setIconName(c.getIconName());
        dto.setCustom(c.isCustom());
        return dto;
    }
}
