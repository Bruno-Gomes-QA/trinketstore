package com.reducess.trinketstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "ID do usuário é obrigatório")
    private Integer userId;

    @Pattern(regexp = "pending|paid|canceled|fulfilled|picked_up", message = "Status deve ser: pending, paid, canceled, fulfilled ou picked_up")
    private String statusOrder = "pending";

    @NotNull(message = "Total do pedido é obrigatório")
    private Integer totalOrders;

    @NotBlank(message = "Moeda é obrigatória")
    @Size(max = 10, message = "Moeda não pode ter mais de 10 caracteres")
    private String currencyOrder = "BRL";

    @NotBlank(message = "ID do checkout é obrigatório")
    @Size(max = 100, message = "ID do checkout não pode ter mais de 100 caracteres")
    private String checkoutId;

    @NotBlank(message = "Payment intent é obrigatório")
    @Size(max = 100, message = "Payment intent não pode ter mais de 100 caracteres")
    private String paymentIntent;

    @Size(max = 300, message = "Token QR não pode ter mais de 300 caracteres")
    private String pickupQrToken;

    @Valid
    private List<CreateOrderItemRequest> items;
}

