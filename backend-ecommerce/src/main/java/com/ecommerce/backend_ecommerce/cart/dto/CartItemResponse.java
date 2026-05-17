package com.ecommerce.backend_ecommerce.cart.dto;

import com.ecommerce.backend_ecommerce.cart.entity.CartItemEntity;

import java.math.BigDecimal;

public record CartItemResponse(
    Long itemId,
    Long productId,
    String productTitle,
    String thumbnail,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal subtotal
) {
    public static CartItemResponse from(CartItemEntity item) {
        BigDecimal subtotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getTitle(),
                item.getProduct().getThumbnail(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                subtotal
        );
    }
}
