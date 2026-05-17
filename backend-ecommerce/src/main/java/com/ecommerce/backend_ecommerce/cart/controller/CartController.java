package com.ecommerce.backend_ecommerce.cart.controller;

import com.ecommerce.backend_ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.backend_ecommerce.cart.dto.CartResponse;
import com.ecommerce.backend_ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.backend_ecommerce.cart.service.CartService;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * GET /api/cart
     * Lấy giỏ hàng của user hiện tại (tạo mới nếu chưa có).
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(cartService.getCart(currentUser));
    }

    /**
     * POST /api/cart/items
     * Thêm sản phẩm vào giỏ. Nếu đã có thì cộng thêm quantity.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal UserEntity currentUser,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(currentUser, request));
    }

    /**
     * PUT /api/cart/items/{itemId}
     * Cập nhật số lượng của 1 item trong giỏ.
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(currentUser, itemId, request));
    }

    /**
     * DELETE /api/cart/items/{itemId}
     * Xoá 1 item khỏi giỏ hàng.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeCartItem(currentUser, itemId));
    }

    /**
     * DELETE /api/cart
     * Xoá toàn bộ giỏ hàng.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserEntity currentUser) {
        cartService.clearCart(currentUser);
        return ResponseEntity.noContent().build();
    }
}
