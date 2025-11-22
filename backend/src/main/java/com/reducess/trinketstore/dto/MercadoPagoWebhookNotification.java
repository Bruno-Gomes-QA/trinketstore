package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoWebhookNotification {

    private String id;
    private String type;
    private String action;
    private WebhookData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookData {
        private String id;
    }
}
