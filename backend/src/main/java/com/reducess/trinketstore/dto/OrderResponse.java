package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Integer idOrder;
    private Integer userId;
    private String statusOrder;
    private Integer totalOrders;
    private String currencyOrder;
    private String checkoutId;
    private String paymentIntent;
    private String pickupQrToken;
    private String pixQrCodeBase64;
    private OffsetDateTime pixExpiresAt;
    private OffsetDateTime createdAt;
    private List<OrderItemResponse> items;
}
