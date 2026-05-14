package com.ecommerce.backend_ecommerce.user.service;

import com.ecommerce.backend_ecommerce.user.dto.UserResponse;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import com.ecommerce.backend_ecommerce.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
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
