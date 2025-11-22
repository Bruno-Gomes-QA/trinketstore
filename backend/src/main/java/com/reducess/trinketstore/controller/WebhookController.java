package com.reducess.trinketstore.controller;

import com.reducess.trinketstore.dto.MercadoPagoWebhookNotification;
import com.reducess.trinketstore.service.PixPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Recebe notificações externas de provedores")
public class WebhookController {

    private final PixPaymentService pixPaymentService;

    @PostMapping("/mercadopago")
    @Operation(summary = "Webhook Mercado Pago", description = "Atualiza pedidos conforme notificações de pagamento PIX")
    public ResponseEntity<Void> handleMercadoPagoNotification(@RequestBody MercadoPagoWebhookNotification notification) {
        log.info("[pix] webhook recebido: {} {}", notification.getType(), notification.getAction());
        pixPaymentService.processWebhook(notification);
        return ResponseEntity.ok().build();
    }
}
