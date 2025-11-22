package com.reducess.trinketstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPaymentResponse {

    private Long id;
    private String status;

    @JsonProperty("external_reference")
    private String externalReference;

    @JsonProperty("date_of_expiration")
    private String dateOfExpiration;

    @JsonProperty("point_of_interaction")
    private PointOfInteraction pointOfInteraction;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointOfInteraction {

        @JsonProperty("transaction_data")
        private TransactionData transactionData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionData {

        @JsonProperty("qr_code")
        private String qrCode;

        @JsonProperty("qr_code_base64")
        private String qrCodeBase64;

        @JsonProperty("ticket_url")
        private String ticketUrl;
    }
}
