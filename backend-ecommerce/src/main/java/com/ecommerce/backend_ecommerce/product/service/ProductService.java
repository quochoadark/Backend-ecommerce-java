package com.ecommerce.backend_ecommerce.product.service;

import com.ecommerce.backend_ecommerce.category.entity.CategoryEntity;
import com.ecommerce.backend_ecommerce.category.service.CategoryService;
import com.ecommerce.backend_ecommerce.product.dto.*;
import com.ecommerce.backend_ecommerce.product.entity.ProductEntity;
import com.ecommerce.backend_ecommerce.product.entity.ProductImageEntity;
import com.ecommerce.backend_ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.backend_ecommerce.product.repository.ProductImageRepository;
import com.ecommerce.backend_ecommerce.product.repository.ProductRepository;
import com.ecommerce.backend_ecommerce.product.specification.ProductSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          CategoryService categoryService,
                          FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    // ─── Lấy danh sách sản phẩm có filter & pagination ──────────────────────

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getProducts(
            Integer categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ProductEntity> spec = Specification
                .where(ProductSpecification.hasCategory(categoryId))
                .and(ProductSpecification.priceGreaterThanOrEqual(minPrice))
                .and(ProductSpecification.priceLessThanOrEqual(maxPrice))
                .and(ProductSpecification.titleContains(keyword));

        return productRepository.findAll(spec, pageable)
                .map(ProductSummaryResponse::from);
    }

    // ─── Lấy chi tiết 1 sản phẩm ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        ProductEntity entity = findByIdOrThrow(id);
        return ProductResponse.from(entity);
    }

    // ─── Tạo sản phẩm mới ───────────────────────────────────────────────────

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        CategoryEntity category = categoryService.findByIdOrThrow(request.categoryId());

        ProductEntity entity = new ProductEntity();
        entity.setTitle(request.title());
        entity.setPrice(request.price());
        entity.setDescription(request.description());
        entity.setThumbnail(request.thumbnail());
        entity.setStockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0);
        entity.setCategory(category);

        ProductEntity saved = productRepository.save(entity);
        log.info("product_created id={} title={}", saved.getId(), saved.getTitle());
        return ProductResponse.from(saved);
    }

    // ─── Cập nhật sản phẩm ──────────────────────────────────────────────────

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        ProductEntity entity = findByIdOrThrow(id);

        if (request.title() != null) entity.setTitle(request.title());
        if (request.price() != null) entity.setPrice(request.price());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.thumbnail() != null) entity.setThumbnail(request.thumbnail());
        if (request.stockQuantity() != null) entity.setStockQuantity(request.stockQuantity());
        if (request.categoryId() != null) {
            CategoryEntity category = categoryService.findByIdOrThrow(request.categoryId());
            entity.setCategory(category);
        }

        ProductEntity saved = productRepository.save(entity);
        log.info("product_updated id={}", saved.getId());
        return ProductResponse.from(saved);
    }

    // ─── Xoá sản phẩm ───────────────────────────────────────────────────────

    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity entity = findByIdOrThrow(id);
        // Xoá file ảnh thumbnail nếu có
        if (entity.getThumbnail() != null) {
            fileStorageService.deleteFile(entity.getThumbnail());
        }
        // Xoá tất cả ảnh phụ
        entity.getImages().forEach(img -> fileStorageService.deleteFile(img.getImageUrl()));

        productRepository.delete(entity);
        log.info("product_deleted id={}", id);
    }

    // ─── Upload ảnh sản phẩm ────────────────────────────────────────────────

    @Transactional
    public ProductResponse uploadProductImage(Long productId, MultipartFile file) {
        ProductEntity entity = findByIdOrThrow(productId);
        String imageUrl = fileStorageService.storeFile(file);

        // Nếu chưa có thumbnail thì set luôn
        if (entity.getThumbnail() == null || entity.getThumbnail().isBlank()) {
            entity.setThumbnail(imageUrl);
        }

        ProductImageEntity imageEntity = new ProductImageEntity(entity, imageUrl);
        productImageRepository.save(imageEntity);
        entity.getImages().add(imageEntity);

        log.info("product_image_uploaded productId={} url={}", productId, imageUrl);
        return ProductResponse.from(entity);
    }

    // ─── Xoá ảnh sản phẩm ───────────────────────────────────────────────────

    @Transactional
    public void deleteProductImage(Long productId, Long imageId) {
        findByIdOrThrow(productId); // kiểm tra product tồn tại

        ProductImageEntity image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to this product");
        }

        fileStorageService.deleteFile(image.getImageUrl());
        productImageRepository.delete(image);
        log.info("product_image_deleted productId={} imageId={}", productId, imageId);
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    public ProductEntity findByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
