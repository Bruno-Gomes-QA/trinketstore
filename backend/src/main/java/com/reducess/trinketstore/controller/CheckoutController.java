package com.reducess.trinketstore.controller;

import com.reducess.trinketstore.dto.PixCheckoutRequest;
import com.reducess.trinketstore.dto.PixCheckoutResponse;
import com.reducess.trinketstore.service.PixPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Fluxo de checkout PIX com Mercado Pago")
public class CheckoutController {

    private final PixPaymentService pixPaymentService;

    @PostMapping("/pix")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Gerar pagamento PIX", description = "Cria um pagamento PIX no Mercado Pago e registra o pedido")
    public ResponseEntity<PixCheckoutResponse> createPixCheckout(@Valid @RequestBody PixCheckoutRequest request) {
        PixCheckoutResponse response = pixPaymentService.createPixCheckout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pix/{paymentId}/refresh")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Atualizar status do PIX", description = "Consulta o Mercado Pago e sincroniza o pedido local sem esperar o polling")
    public ResponseEntity<PixCheckoutResponse> refreshPixPayment(@PathVariable String paymentId) {
        PixCheckoutResponse response = pixPaymentService.refreshPixPayment(paymentId);
        return ResponseEntity.ok(response);
    }
}
