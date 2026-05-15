package com.ecommerce.backend_ecommerce.auth.dto;

import com.ecommerce.backend_ecommerce.user.entity.UserEntity;

import java.time.LocalDateTime;

/**
 * Trả về sau login/register: access token + refresh token + thông tin user.
 * Theo skill springboot-patterns: DTO tự factory method từ entity.
 */
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long userId,
    String email,
    String fullName,
    String role
) {
    /** Convenience: tạo từ entity + tokens */
    public static AuthResponse of(UserEntity user, String accessToken, String refreshToken) {
        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().getName()
        );
    }

    /** Backward-compat: chỉ access token (dùng trong refresh-token response) */
    public static AuthResponse accessOnly(UserEntity user, String accessToken, String refreshToken) {
        return of(user, accessToken, refreshToken);
    }
}
