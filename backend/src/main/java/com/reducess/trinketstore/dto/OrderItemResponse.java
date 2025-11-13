package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Integer idItems;
    private Integer orderId;
    private Integer productId;
    private Integer qtyItems;
    private Integer unitAmount;
    private Integer subtotalAmount;
}

