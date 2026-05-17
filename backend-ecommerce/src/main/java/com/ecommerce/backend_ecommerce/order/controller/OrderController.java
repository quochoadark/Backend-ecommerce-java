package com.ecommerce.backend_ecommerce.order.controller;

import com.ecommerce.backend_ecommerce.order.dto.*;
import com.ecommerce.backend_ecommerce.order.service.OrderService;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ─── USER endpoints ───────────────────────────────────────────────────────

    /**
     * POST /api/orders
     * Tạo đơn hàng mới (COD). Tự động xoá cart sau khi đặt hàng thành công.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserEntity currentUser,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(currentUser, request));
    }

    /**
     * GET /api/orders/my?page=0&size=10
     * Lấy danh sách đơn hàng của chính mình.
     */
    @GetMapping("/my")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getMyOrders(currentUser, page, size));
    }

    /**
     * GET /api/orders/my/{id}
     * Xem chi tiết 1 đơn hàng của mình.
     */
    @GetMapping("/my/{id}")
    public ResponseEntity<OrderResponse> getMyOrderById(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getMyOrderById(currentUser, id));
    }

    /**
     * PATCH /api/orders/my/{id}/cancel
     * Huỷ đơn hàng (chỉ khi đang PENDING). Hoàn trả stock sản phẩm.
     */
    @PatchMapping("/my/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(currentUser, id));
    }

    // ─── ADMIN endpoints ──────────────────────────────────────────────────────

    /**
     * GET /api/orders?page=0&size=20
     * Lấy tất cả đơn hàng — Chỉ ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size));
    }

    /**
     * GET /api/orders/{id}
     * Xem chi tiết bất kỳ đơn hàng — Chỉ ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * PATCH /api/orders/{id}/status
     * Cập nhật trạng thái đơn hàng — Chỉ ADMIN.
     * Body: {"status": "CONFIRMED"} (PENDING|CONFIRMED|SHIPPING|DELIVERED|CANCELLED)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }
}
