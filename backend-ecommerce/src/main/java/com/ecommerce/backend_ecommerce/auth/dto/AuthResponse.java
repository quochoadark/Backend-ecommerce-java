package com.ecommerce.backend_ecommerce.auth.dto;

public record AuthResponse(
    String token,
    String type
) {
    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}
