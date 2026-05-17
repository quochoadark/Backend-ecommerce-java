package com.ecommerce.backend_ecommerce.order.entity;

import com.ecommerce.backend_ecommerce.product.entity.ProductEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_details")
public class OrderDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "number_of_products", nullable = false)
    private Integer numberOfProducts;

    @Column(name = "total_money", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalMoney;

    public OrderDetailEntity() {}

    public OrderDetailEntity(OrderEntity order, ProductEntity product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.price = product.getPrice();
        this.numberOfProducts = quantity;
        this.totalMoney = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public void setOrder(OrderEntity order) { this.order = order; }
    public ProductEntity getProduct() { return product; }
    public BigDecimal getPrice() { return price; }
    public Integer getNumberOfProducts() { return numberOfProducts; }
    public BigDecimal getTotalMoney() { return totalMoney; }
}
