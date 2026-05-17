package com.ecommerce.backend_ecommerce.order.dto;

import com.ecommerce.backend_ecommerce.order.entity.OrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    Long userId,
    String fullname,
    String phoneNumber,
    String address,
    String note,
    LocalDateTime orderDate,
    String status,
    BigDecimal totalMoney,
    String paymentMethod,
    String paymentStatus,
    List<OrderDetailResponse> items
) {
    public static OrderResponse from(OrderEntity order) {
        List<OrderDetailResponse> items = order.getOrderDetails().stream()
                .map(OrderDetailResponse::from)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getFullname(),
                order.getPhoneNumber(),
                order.getAddress(),
                order.getNote(),
                order.getOrderDate(),
                order.getStatus().name(),
                order.getTotalMoney(),
                order.getPaymentMethod().name(),
                order.getPaymentStatus(),
                items
        );
    }
}
