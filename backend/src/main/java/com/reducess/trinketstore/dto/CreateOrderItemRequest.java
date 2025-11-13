package com.reducess.trinketstore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemRequest {

    @NotNull(message = "ID do produto é obrigatório")
    private Integer productId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer qtyItems;

    @NotNull(message = "Valor unitário é obrigatório")
    private Integer unitAmount;

    @NotNull(message = "Subtotal é obrigatório")
    private Integer subtotalAmount;
}

