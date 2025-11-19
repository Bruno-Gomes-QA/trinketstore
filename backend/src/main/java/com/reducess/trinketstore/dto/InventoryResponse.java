package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Integer idInventory;
    private Integer productId;
    private Integer qtyOnHand;
    private String nomeProduct;
    private String slugProduct;
    private String categoriaProduct;
    private Boolean ativoProduct;
    private String imagemurlProduct;
}
