package com.reducess.trinketstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_items")
    private Integer idItems;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "qty_items", nullable = false)
    private Integer qtyItems;

    @Column(name = "unit_amount", nullable = false)
    private Integer unitAmount;

    @Column(name = "subtotal_amount", nullable = false)
    private Integer subtotalAmount;
}

