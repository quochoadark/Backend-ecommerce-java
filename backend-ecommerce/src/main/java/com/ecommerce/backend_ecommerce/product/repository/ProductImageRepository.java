package com.ecommerce.backend_ecommerce.product.repository;

import com.ecommerce.backend_ecommerce.product.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    List<ProductImageEntity> findByProductId(Long productId);

    void deleteByProductIdAndId(Long productId, Long imageId);
}
