package com.ecommerce.backend_ecommerce.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "Title must not be blank")
    @Size(max = 350, message = "Title must not exceed 350 characters")
    String title,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal price,

    @NotNull(message = "Category ID is required")
    Integer categoryId,

    String description,

    String thumbnail,

    @Min(value = 0, message = "Stock quantity must be >= 0")
    Integer stockQuantity
) {}
