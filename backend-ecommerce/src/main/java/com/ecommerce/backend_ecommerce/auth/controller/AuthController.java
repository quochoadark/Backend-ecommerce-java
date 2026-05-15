package com.ecommerce.backend_ecommerce.auth.controller;

import com.ecommerce.backend_ecommerce.auth.dto.*;
import com.ecommerce.backend_ecommerce.auth.service.AuthService;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * Đăng ký tài khoản mới. Trả về access token + refresh token + thông tin user.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * POST /api/auth/login
     * Đăng nhập. Trả về access token + refresh token + thông tin user.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/refresh-token
     * Dùng refresh token để lấy access token mới (token rotation).
     * Refresh token cũ sẽ bị revoke, trả về refresh token mới.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * POST /api/auth/logout
     * Đăng xuất: revoke tất cả refresh token của user hiện tại.
     * Yêu cầu Bearer token hợp lệ trong header.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserEntity currentUser) {
        authService.logout(currentUser.getEmail());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/auth/forgot-password
     * Yêu cầu đặt lại mật khẩu. Luôn trả về 200 để tránh user enumeration.
     * (Trong production: gửi email chứa reset link)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    /**
     * POST /api/auth/reset-password
     * Đặt lại mật khẩu bằng reset token (single-use, hết hạn sau 15 phút).
     * Sau khi đổi mật khẩu thành công: tất cả refresh token bị revoke.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. Please login again."));
    }
}
