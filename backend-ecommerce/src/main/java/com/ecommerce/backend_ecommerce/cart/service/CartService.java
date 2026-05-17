package com.ecommerce.backend_ecommerce.cart.service;

import com.ecommerce.backend_ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.backend_ecommerce.cart.dto.CartResponse;
import com.ecommerce.backend_ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.backend_ecommerce.cart.entity.CartEntity;
import com.ecommerce.backend_ecommerce.cart.entity.CartItemEntity;
import com.ecommerce.backend_ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.backend_ecommerce.cart.repository.CartRepository;
import com.ecommerce.backend_ecommerce.product.entity.ProductEntity;
import com.ecommerce.backend_ecommerce.product.service.ProductService;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    // ─── Lấy giỏ hàng (tạo mới nếu chưa có) ────────────────────────────────

    @Transactional
    public CartResponse getCart(UserEntity currentUser) {
        CartEntity cart = getOrCreateCart(currentUser);
        return CartResponse.from(cart);
    }

    // ─── Thêm sản phẩm vào giỏ ──────────────────────────────────────────────

    @Transactional
    public CartResponse addToCart(UserEntity currentUser, AddToCartRequest request) {
        CartEntity cart = getOrCreateCart(currentUser);
        ProductEntity product = productService.findByIdOrThrow(request.productId());

        if (product.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Product is out of stock: " + product.getTitle());
        }

        // Nếu đã có sản phẩm trong giỏ → cộng thêm quantity
        CartItemEntity existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.quantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItemEntity newItem = new CartItemEntity(cart, product, request.quantity());
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        log.info("cart_item_added userId={} productId={} quantity={}",
                currentUser.getId(), request.productId(), request.quantity());
        return CartResponse.from(cart);
    }

    // ─── Cập nhật số lượng item ──────────────────────────────────────────────

    @Transactional
    public CartResponse updateCartItem(UserEntity currentUser, Long itemId, UpdateCartItemRequest request) {
        CartEntity cart = getOrCreateCart(currentUser);
        CartItemEntity item = findItemInCart(cart, itemId);
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        log.info("cart_item_updated userId={} itemId={} quantity={}",
                currentUser.getId(), itemId, request.quantity());
        return CartResponse.from(cart);
    }

    // ─── Xoá 1 item khỏi giỏ ────────────────────────────────────────────────

    @Transactional
    public CartResponse removeCartItem(UserEntity currentUser, Long itemId) {
        CartEntity cart = getOrCreateCart(currentUser);
        CartItemEntity item = findItemInCart(cart, itemId);
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        log.info("cart_item_removed userId={} itemId={}", currentUser.getId(), itemId);
        return CartResponse.from(cart);
    }

    // ─── Xoá toàn bộ giỏ hàng ───────────────────────────────────────────────

    @Transactional
    public void clearCart(UserEntity currentUser) {
        CartEntity cart = getOrCreateCart(currentUser);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("cart_cleared userId={}", currentUser.getId());
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private CartEntity getOrCreateCart(UserEntity user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartItemEntity findItemInCart(CartEntity cart, Long itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));
    }
}
