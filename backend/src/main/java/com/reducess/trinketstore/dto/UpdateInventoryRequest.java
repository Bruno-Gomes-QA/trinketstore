package com.reducess.trinketstore.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryRequest {

    private Integer productId;

    @Min(value = 0, message = "Quantidade em estoque não pode ser negativa")
    private Integer qtyOnHand;
}

