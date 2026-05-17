package com.ecommerce.backend_ecommerce.product.repository;

import com.ecommerce.backend_ecommerce.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>,
        JpaSpecificationExecutor<ProductEntity> {

    Page<ProductEntity> findByCategoryId(Integer categoryId, Pageable pageable);

    boolean existsByTitle(String title);
}
