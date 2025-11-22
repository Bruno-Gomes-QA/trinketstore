package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PixCheckoutResponse {

    private OrderResponse order;
    private PixPaymentDetails pix;
}
