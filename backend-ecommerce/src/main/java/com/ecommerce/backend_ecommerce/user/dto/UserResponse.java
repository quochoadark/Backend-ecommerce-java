package com.ecommerce.backend_ecommerce.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    String phoneNumber,
    String address,
    String role,
    Boolean isActive,
    LocalDateTime createdAt
) {}
