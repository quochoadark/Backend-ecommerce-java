package com.ecommerce.backend_ecommerce.category.repository;

import com.ecommerce.backend_ecommerce.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {

    boolean existsByName(String name);

    Optional<CategoryEntity> findByName(String name);
}
