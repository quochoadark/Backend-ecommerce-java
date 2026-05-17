package com.ecommerce.backend_ecommerce.cart.dto;

import com.ecommerce.backend_ecommerce.cart.entity.CartEntity;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
    Long cartId,
    List<CartItemResponse> items,
    Integer totalItems,
    BigDecimal totalAmount
) {
    public static CartResponse from(CartEntity cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), itemResponses, totalItems, totalAmount);
    }
}
