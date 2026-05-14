package com.ecommerce.backend_ecommerce.user.controller;

import com.ecommerce.backend_ecommerce.user.dto.UserResponse;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import com.ecommerce.backend_ecommerce.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(userService.getUserProfile(user.getEmail()));
    }
}
