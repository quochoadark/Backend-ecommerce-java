package com.ecommerce.backend_ecommerce.order.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Status is required")
    String status
) {}
