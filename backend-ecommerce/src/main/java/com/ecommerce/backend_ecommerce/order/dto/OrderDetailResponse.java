package com.ecommerce.backend_ecommerce.order.dto;

import com.ecommerce.backend_ecommerce.order.entity.OrderDetailEntity;

import java.math.BigDecimal;

public record OrderDetailResponse(
    Long id,
    Long productId,
    String productTitle,
    String productThumbnail,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal totalMoney
) {
    public static OrderDetailResponse from(OrderDetailEntity detail) {
        return new OrderDetailResponse(
                detail.getId(),
                detail.getProduct().getId(),
                detail.getProduct().getTitle(),
                detail.getProduct().getThumbnail(),
                detail.getPrice(),
                detail.getNumberOfProducts(),
                detail.getTotalMoney()
        );
    }
}
