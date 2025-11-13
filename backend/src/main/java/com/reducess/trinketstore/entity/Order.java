package com.reducess.trinketstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_order")
    private Integer idOrder;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "status_order", nullable = false)
    private String statusOrder = "pending";

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Column(name = "currency_order", nullable = false, length = 10)
    private String currencyOrder = "BRL";

    @Column(name = "checkout_id", nullable = false, length = 100, unique = true)
    private String checkoutId;

    @Column(name = "payment_intent", nullable = false, length = 100, unique = true)
    private String paymentIntent;

    @Column(name = "pickup_qr_token", length = 300)
    private String pickupQrToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (statusOrder == null) {
            statusOrder = "pending";
        }
        if (currencyOrder == null) {
            currencyOrder = "BRL";
        }
    }
}

