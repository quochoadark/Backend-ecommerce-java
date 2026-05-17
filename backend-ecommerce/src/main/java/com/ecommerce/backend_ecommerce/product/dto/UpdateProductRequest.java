package com.ecommerce.backend_ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
    @Size(max = 350, message = "Title must not exceed 350 characters")
    String title,

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal price,

    Integer categoryId,

    String description,

    String thumbnail,

    @Min(value = 0, message = "Stock quantity must be >= 0")
    Integer stockQuantity
) {}
