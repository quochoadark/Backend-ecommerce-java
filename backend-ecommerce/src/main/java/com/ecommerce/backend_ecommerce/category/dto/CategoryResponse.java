package com.ecommerce.backend_ecommerce.category.dto;

import com.ecommerce.backend_ecommerce.category.entity.CategoryEntity;

public record CategoryResponse(
    Integer id,
    String name
) {
    public static CategoryResponse from(CategoryEntity entity) {
        return new CategoryResponse(entity.getId(), entity.getName());
    }
}
