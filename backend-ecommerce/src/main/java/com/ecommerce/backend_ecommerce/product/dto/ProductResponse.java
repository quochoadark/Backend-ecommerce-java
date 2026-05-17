package com.ecommerce.backend_ecommerce.product.dto;

import com.ecommerce.backend_ecommerce.product.entity.ProductEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
    Long id,
    String title,
    BigDecimal price,
    String thumbnail,
    String description,
    Integer stockQuantity,
    Integer categoryId,
    String categoryName,
    List<String> imageUrls,
    LocalDateTime createdAt
) {
    public static ProductResponse from(ProductEntity entity) {
        List<String> urls = entity.getImages().stream()
                .map(img -> img.getImageUrl())
                .toList();
        return new ProductResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getPrice(),
                entity.getThumbnail(),
                entity.getDescription(),
                entity.getStockQuantity(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                urls,
                entity.getCreatedAt()
        );
    }
}
