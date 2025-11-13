package com.reducess.trinketstore.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {
    private Integer idPrice;
    private Integer productId;
    private Integer amountPrice;
    private String currencyPrice;
    private Boolean vigentePrice;
    private String productName;
}
