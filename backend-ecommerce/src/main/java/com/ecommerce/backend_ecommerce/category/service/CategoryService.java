package com.ecommerce.backend_ecommerce.category.service;

import com.ecommerce.backend_ecommerce.category.dto.CategoryResponse;
import com.ecommerce.backend_ecommerce.category.dto.CreateCategoryRequest;
import com.ecommerce.backend_ecommerce.category.dto.UpdateCategoryRequest;
import com.ecommerce.backend_ecommerce.category.entity.CategoryEntity;
import com.ecommerce.backend_ecommerce.category.exception.CategoryNotFoundException;
import com.ecommerce.backend_ecommerce.category.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ─── Lấy tất cả danh mục ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(CategoryResponse::from)
                .toList();
    }

    // ─── Lấy danh mục theo ID ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer id) {
        CategoryEntity entity = findByIdOrThrow(id);
        return CategoryResponse.from(entity);
    }

    // ─── Tạo danh mục mới ───────────────────────────────────────────────────

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Category already exists with name: " + request.name());
        }
        CategoryEntity entity = new CategoryEntity(request.name());
        CategoryEntity saved = categoryRepository.save(entity);
        log.info("category_created id={} name={}", saved.getId(), saved.getName());
        return CategoryResponse.from(saved);
    }

    // ─── Cập nhật danh mục ──────────────────────────────────────────────────

    @Transactional
    public CategoryResponse updateCategory(Integer id, UpdateCategoryRequest request) {
        CategoryEntity entity = findByIdOrThrow(id);

        if (!entity.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Category already exists with name: " + request.name());
        }

        entity.setName(request.name());
        CategoryEntity saved = categoryRepository.save(entity);
        log.info("category_updated id={} name={}", saved.getId(), saved.getName());
        return CategoryResponse.from(saved);
    }

    // ─── Xoá danh mục ───────────────────────────────────────────────────────

    @Transactional
    public void deleteCategory(Integer id) {
        CategoryEntity entity = findByIdOrThrow(id);
        categoryRepository.delete(entity);
        log.info("category_deleted id={}", id);
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    public CategoryEntity findByIdOrThrow(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
