package com.reducess.trinketstore.dto;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceRequest {
    @Positive(message = "Valor do preço deve ser positivo")
    private Integer amountPrice;
    @Size(max = 10, message = "Moeda não pode ter mais de 10 caracteres")
    private String currencyPrice;
    private Boolean vigentePrice;
}
