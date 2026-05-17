package com.ecommerce.backend_ecommerce.product.dto;

import com.ecommerce.backend_ecommerce.product.entity.ProductEntity;

import java.math.BigDecimal;

public record ProductSummaryResponse(
    Long id,
    String title,
    BigDecimal price,
    String thumbnail,
    Integer stockQuantity,
    Integer categoryId,
    String categoryName
) {
    public static ProductSummaryResponse from(ProductEntity entity) {
        return new ProductSummaryResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getPrice(),
                entity.getThumbnail(),
                entity.getStockQuantity(),
                entity.getCategory().getId(),
                entity.getCategory().getName()
        );
    }
}
