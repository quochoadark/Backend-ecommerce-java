package com.ecommerce.backend_ecommerce.auth.service;

import com.ecommerce.backend_ecommerce.auth.dto.*;
import com.ecommerce.backend_ecommerce.auth.entity.PasswordResetToken;
import com.ecommerce.backend_ecommerce.auth.entity.RefreshToken;
import com.ecommerce.backend_ecommerce.auth.repository.PasswordResetTokenRepository;
import com.ecommerce.backend_ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.backend_ecommerce.security.JwtService;
import com.ecommerce.backend_ecommerce.user.entity.RoleEntity;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import com.ecommerce.backend_ecommerce.user.repository.RoleRepository;
import com.ecommerce.backend_ecommerce.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // Refresh token sống 7 ngày; reset password token sống 15 phút
    @Value("${auth.refresh-token.expiration-days:7}")
    private int refreshTokenExpirationDays;

    @Value("${auth.reset-password.expiration-minutes:15}")
    private int resetPasswordExpirationMinutes;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // ─── Register ────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        RoleEntity role = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role USER not found in database"));

        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());
        user.setRole(role);

        userRepository.save(user);
        log.info("register email={}", request.email());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user);
        return AuthResponse.of(user, accessToken, refreshToken);
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        log.info("login email={}", request.email());

        // Revoke old refresh tokens trước khi cấp token mới (token rotation)
        refreshTokenRepository.revokeAllByUserId(user.getId());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user);
        return AuthResponse.of(user, accessToken, refreshToken);
    }

    // ─── Refresh Token ───────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (stored.getRevoked()) {
            log.warn("refresh_token_reuse_detected userId={}", stored.getUser().getId());
            // Phát hiện token đã bị revoke được dùng lại → revoke tất cả (bảo vệ tấn công)
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        if (stored.isExpired()) {
            throw new BadCredentialsException("Refresh token has expired, please login again");
        }

        UserEntity user = stored.getUser();

        // Token rotation: revoke token cũ, cấp token mới
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = createRefreshToken(user);

        log.info("refresh_token_rotated userId={}", user.getId());
        return AuthResponse.of(user, newAccessToken, newRefreshToken);
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("logout email={}", email);
    }

    // ─── Forgot Password ─────────────────────────────────────────────────────

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        // Không tiết lộ user có tồn tại hay không (security best practice)
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            // Huỷ token cũ chưa dùng
            passwordResetTokenRepository.invalidateAllByUserId(user.getId());

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(resetPasswordExpirationMinutes));
            passwordResetTokenRepository.save(resetToken);

            log.info("forgot_password_token_created email={}", request.email());
            // Nếu có email service: emailService.sendResetEmail(user.getEmail(), resetToken.getToken());
        });

        // Luôn trả về thông báo generic để tránh user enumeration
        return "If an account with that email exists, a password reset link has been sent.";
    }

    // ─── Reset Password ──────────────────────────────────────────────────────

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.resetToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));

        if (resetToken.getUsed()) {
            throw new BadCredentialsException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new BadCredentialsException("Reset token has expired");
        }

        UserEntity user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Đánh dấu token đã dùng
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke tất cả refresh tokens (buộc đăng nhập lại sau đổi mật khẩu)
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("reset_password_success userId={}", user.getId());
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private String createRefreshToken(UserEntity user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
