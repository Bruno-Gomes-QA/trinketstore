package com.reducess.trinketstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {

    @Pattern(regexp = "pending|paid|canceled|fulfilled|picked_up", message = "Status deve ser: pending, paid, canceled, fulfilled ou picked_up")
    private String statusOrder;

    private Integer totalOrders;

    @Size(max = 10, message = "Moeda não pode ter mais de 10 caracteres")
    private String currencyOrder;

    @Size(max = 100, message = "ID do checkout não pode ter mais de 100 caracteres")
    private String checkoutId;

    @Size(max = 100, message = "Payment intent não pode ter mais de 100 caracteres")
    private String paymentIntent;

    private String pickupQrToken;

    private String pixQrCodeBase64;

    private OffsetDateTime pixExpiresAt;

    @Valid
    private List<CreateOrderItemRequest> items;
}
