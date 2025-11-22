package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PixPaymentDetails {

    private String paymentId;
    private String status;
    private String qrCode;
    private String qrCodeBase64;
    private OffsetDateTime expiresAt;
    private String checkoutId;
}
