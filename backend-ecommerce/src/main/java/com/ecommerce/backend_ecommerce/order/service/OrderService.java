package com.ecommerce.backend_ecommerce.order.service;

import com.ecommerce.backend_ecommerce.cart.entity.CartEntity;
import com.ecommerce.backend_ecommerce.cart.repository.CartRepository;
import com.ecommerce.backend_ecommerce.order.dto.*;
import com.ecommerce.backend_ecommerce.order.entity.*;
import com.ecommerce.backend_ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.backend_ecommerce.order.repository.OrderRepository;
import com.ecommerce.backend_ecommerce.product.entity.ProductEntity;
import com.ecommerce.backend_ecommerce.product.service.ProductService;
import com.ecommerce.backend_ecommerce.user.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductService productService,
                        CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.cartRepository = cartRepository;
    }

    // ─── Tạo đơn hàng mới ───────────────────────────────────────────────────

    @Transactional
    public OrderResponse createOrder(UserEntity currentUser, CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setUser(currentUser);
        order.setFullname(request.fullname());
        order.setPhoneNumber(request.phoneNumber());
        order.setAddress(request.address());
        order.setNote(request.note());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setPaymentStatus("PENDING");

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest item : request.items()) {
            ProductEntity product = productService.findByIdOrThrow(item.productId());

            if (product.getStockQuantity() < item.quantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: " + product.getTitle()
                        + ". Available: " + product.getStockQuantity());
            }

            // Trừ stock
            product.setStockQuantity(product.getStockQuantity() - item.quantity());

            OrderDetailEntity detail = new OrderDetailEntity(order, product, item.quantity());
            order.getOrderDetails().add(detail);
            total = total.add(detail.getTotalMoney());
        }

        order.setTotalMoney(total);
        OrderEntity saved = orderRepository.save(order);

        // Xoá cart sau khi đặt hàng thành công
        cartRepository.findByUserId(currentUser.getId())
                .ifPresent(cart -> {
                    cart.getItems().clear();
                    cartRepository.save(cart);
                });

        log.info("order_created id={} userId={} total={}", saved.getId(), currentUser.getId(), total);
        return OrderResponse.from(saved);
    }

    // ─── USER: Lấy đơn hàng của mình ────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(UserEntity currentUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findByUserId(currentUser.getId(), pageable)
                .map(OrderResponse::from);
    }

    // ─── USER: Chi tiết 1 đơn của mình ─────────────────────────────────────

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(UserEntity currentUser, Long orderId) {
        OrderEntity order = findByIdOrThrow(orderId);
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You don't have access to this order");
        }
        return OrderResponse.from(order);
    }

    // ─── USER: Huỷ đơn (chỉ khi PENDING) ───────────────────────────────────

    @Transactional
    public OrderResponse cancelOrder(UserEntity currentUser, Long orderId) {
        OrderEntity order = findByIdOrThrow(orderId);

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You don't have access to this order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be cancelled");
        }

        // Hoàn trả stock
        order.getOrderDetails().forEach(detail -> {
            ProductEntity product = detail.getProduct();
            product.setStockQuantity(product.getStockQuantity() + detail.getNumberOfProducts());
        });

        order.setStatus(OrderStatus.CANCELLED);
        OrderEntity saved = orderRepository.save(order);
        log.info("order_cancelled id={} userId={}", orderId, currentUser.getId());
        return OrderResponse.from(saved);
    }

    // ─── ADMIN: Lấy tất cả đơn hàng ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    // ─── ADMIN: Xem chi tiết 1 đơn bất kỳ ──────────────────────────────────

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return OrderResponse.from(findByIdOrThrow(orderId));
    }

    // ─── ADMIN: Cập nhật trạng thái đơn ─────────────────────────────────────

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        OrderEntity order = findByIdOrThrow(orderId);

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + request.status()
                    + ". Valid values: PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED");
        }

        order.setStatus(newStatus);

        // Nếu đã giao hàng → đánh dấu thanh toán là PAID (cho COD)
        if (newStatus == OrderStatus.DELIVERED) {
            order.setPaymentStatus("PAID");
        }

        OrderEntity saved = orderRepository.save(order);
        log.info("order_status_updated id={} status={}", orderId, newStatus);
        return OrderResponse.from(saved);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private OrderEntity findByIdOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
