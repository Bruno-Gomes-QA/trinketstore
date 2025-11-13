package com.reducess.trinketstore.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderItemRequest {

    private Integer orderId;

    private Integer productId;

    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer qtyItems;

    private Integer unitAmount;

    private Integer subtotalAmount;
}

