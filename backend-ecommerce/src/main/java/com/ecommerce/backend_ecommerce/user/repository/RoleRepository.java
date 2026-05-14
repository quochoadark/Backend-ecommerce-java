package com.ecommerce.backend_ecommerce.user.repository;

import com.ecommerce.backend_ecommerce.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByName(String name);
}
