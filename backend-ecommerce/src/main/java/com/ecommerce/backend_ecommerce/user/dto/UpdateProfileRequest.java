package com.ecommerce.backend_ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    String fullName,

    @Pattern(regexp = "^(\\+?[0-9]{9,15})?$", message = "Invalid phone number format")
    String phoneNumber,

    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address
) {}
