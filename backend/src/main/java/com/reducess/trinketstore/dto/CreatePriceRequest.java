package com.reducess.trinketstore.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePriceRequest {
    @NotNull(message = "Product ID é obrigatório")
    private Integer productId;
    @NotNull(message = "Valor do preço é obrigatório")
    @Positive(message = "Valor do preço deve ser positivo")
    private Integer amountPrice;
    @NotNull(message = "Moeda é obrigatória")
    @Size(max = 10, message = "Moeda não pode ter mais de 10 caracteres")
    private String currencyPrice;
    @NotNull(message = "Status vigente é obrigatório")
    private Boolean vigentePrice;
}
