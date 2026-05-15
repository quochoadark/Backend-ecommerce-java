package com.ecommerce.backend_ecommerce.user.controller;

import com.ecommerce.backend_ecommerce.user.dto.*;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import com.ecommerce.backend_ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USER endpoints (chỉ cần đăng nhập)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/users/me
     * Lấy thông tin profile của chính mình.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(userService.getUserProfile(currentUser.getEmail()));
    }

    /**
     * PUT /api/users/me
     * Cập nhật profile của chính mình (họ tên, SĐT, địa chỉ).
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserEntity currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(currentUser.getEmail(), request));
    }

    /**
     * PUT /api/users/me/password
     * Đổi mật khẩu.
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserEntity currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getEmail(), request);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN endpoints (chỉ ADMIN mới truy cập được)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/users
     * Lấy danh sách tất cả user.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/users/{id}
     * Xem chi tiết 1 user theo ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * PUT /api/users/{id}
     * Admin cập nhật thông tin user bất kỳ (kể cả role).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminUpdateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(userService.adminUpdateUser(id, request));
    }

    /**
     * DELETE /api/users/{id}
     * Admin xoá user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/users/{id}/toggle-active
     * Admin khoá hoặc mở khoá tài khoản user.
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleUserActive(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserActive(id));
    }
}
