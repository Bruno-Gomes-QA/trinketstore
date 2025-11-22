package com.reducess.trinketstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PixCheckoutRequest {

    @NotNull(message = "ID do usuário é obrigatório")
    private Integer userId;

    @NotNull(message = "O total do pedido é obrigatório")
    @Positive(message = "O total do pedido deve ser maior que zero")
    private Integer totalAmountInCents;

    @Size(max = 10, message = "Moeda não pode ter mais de 10 caracteres")
    private String currency = "BRL";

    private String description;

    @Valid
    @NotEmpty(message = "Informe ao menos um item no checkout")
    private List<CreateOrderItemRequest> items;
}
