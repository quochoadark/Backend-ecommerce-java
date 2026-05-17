package com.ecommerce.backend_ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    String fullname,

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    String phoneNumber,

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    String address,

    @Size(max = 255)
    String note,

    @NotEmpty(message = "Order must have at least one item")
    List<OrderItemRequest> items
) {}
