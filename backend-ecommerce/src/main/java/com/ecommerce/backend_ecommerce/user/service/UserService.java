package com.ecommerce.backend_ecommerce.user.service;

import com.ecommerce.backend_ecommerce.user.dto.*;
import com.ecommerce.backend_ecommerce.user.entity.RoleEntity;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import com.ecommerce.backend_ecommerce.user.repository.RoleRepository;
import com.ecommerce.backend_ecommerce.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── USER: Xem profile bản thân ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        UserEntity user = findByEmailOrThrow(email);
        return toResponse(user);
    }

    // ─── USER: Cập nhật profile bản thân ────────────────────────────────────

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        UserEntity user = findByEmailOrThrow(email);

        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());

        return toResponse(userRepository.save(user));
    }

    // ─── USER: Đổi mật khẩu ─────────────────────────────────────────────────

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        UserEntity user = findByEmailOrThrow(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // ─── ADMIN: Lấy danh sách tất cả users ──────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── ADMIN: Xem chi tiết 1 user theo ID ─────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    // ─── ADMIN: Cập nhật user bất kỳ ────────────────────────────────────────

    @Transactional
    public UserResponse adminUpdateUser(Long id, AdminUpdateUserRequest request) {
        UserEntity user = findByIdOrThrow(id);

        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());

        if (request.role() != null && !request.role().isBlank()) {
            RoleEntity role = roleRepository.findByName(request.role())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + request.role()));
            user.setRole(role);
        }

        return toResponse(userRepository.save(user));
    }

    // ─── ADMIN: Xoá user ────────────────────────────────────────────────────

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ─── ADMIN: Khoá / Mở khoá tài khoản ───────────────────────────────────

    @Transactional
    public UserResponse toggleUserActive(Long id) {
        UserEntity user = findByIdOrThrow(id);
        user.setIsActive(!user.getIsActive());
        return toResponse(userRepository.save(user));
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private UserEntity findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private UserEntity findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getRole().getName(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }
}
