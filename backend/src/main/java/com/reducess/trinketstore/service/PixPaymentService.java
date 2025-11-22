package com.reducess.trinketstore.service;

import com.reducess.trinketstore.dto.CreateOrderRequest;
import com.reducess.trinketstore.dto.MercadoPagoPaymentResponse;
import com.reducess.trinketstore.dto.MercadoPagoWebhookNotification;
import com.reducess.trinketstore.dto.OrderResponse;
import com.reducess.trinketstore.dto.PixCheckoutRequest;
import com.reducess.trinketstore.dto.PixCheckoutResponse;
import com.reducess.trinketstore.dto.PixPaymentDetails;
import com.reducess.trinketstore.entity.Order;
import com.reducess.trinketstore.entity.User;
import com.reducess.trinketstore.exception.PaymentException;
import com.reducess.trinketstore.repository.OrderRepository;
import com.reducess.trinketstore.repository.UserRepository;
import com.reducess.trinketstore.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PixPaymentService {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MercadoPagoClient mercadoPagoClient;
    private final InventoryService inventoryService;

    @Transactional
    public PixCheckoutResponse createPixCheckout(PixCheckoutRequest request) {
        User user = userRepository.findById(request.getUserId().longValue())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String checkoutId = buildCheckoutReference();
        BigDecimal amount = BigDecimal.valueOf(request.getTotalAmountInCents())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        inventoryService.verifyStockAvailability(request.getItems());
        MercadoPagoPaymentResponse paymentResponse = mercadoPagoClient.createPixPayment(
                amount,
                request.getDescription(),
                checkoutId,
                user.getEmail()
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(request.getUserId());
        orderRequest.setStatusOrder("pending");
        orderRequest.setTotalOrders(request.getTotalAmountInCents());
        orderRequest.setCurrencyOrder(StringUtils.hasText(request.getCurrency()) ? request.getCurrency() : "BRL");
        orderRequest.setCheckoutId(checkoutId);
        orderRequest.setPaymentIntent(String.valueOf(paymentResponse.getId()));
        orderRequest.setPickupQrToken(extractQrCode(paymentResponse));
        orderRequest.setPixQrCodeBase64(extractQrCodeBase64(paymentResponse));
        orderRequest.setPixExpiresAt(parseExpiration(paymentResponse.getDateOfExpiration()));
        orderRequest.setItems(request.getItems());

        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        PixPaymentDetails paymentDetails = new PixPaymentDetails(
                orderResponse.getPaymentIntent(),
                paymentResponse.getStatus(),
                orderResponse.getPickupQrToken(),
                orderResponse.getPixQrCodeBase64(),
                orderResponse.getPixExpiresAt(),
                orderResponse.getCheckoutId()
        );

        return new PixCheckoutResponse(orderResponse, paymentDetails);
    }

    @Transactional
    public void processWebhook(MercadoPagoWebhookNotification notification) {
        if (notification == null) {
            log.warn("[pix] Webhook recebido sem payload");
            return;
        }

        String paymentId = Optional.ofNullable(notification.getData())
                .map(MercadoPagoWebhookNotification.WebhookData::getId)
                .orElse(notification.getId());

        if (!StringUtils.hasText(paymentId)) {
            log.warn("[pix] Webhook recebido sem payment id: {}", notification);
            return;
        }

        try {
            MercadoPagoPaymentResponse paymentResponse = mercadoPagoClient.getPayment(paymentId);
            orderRepository.findByPaymentIntent(String.valueOf(paymentResponse.getId()))
                    .ifPresentOrElse(
                            order -> updateOrderFromPayment(order, paymentResponse),
                            () -> log.warn("[pix] Pedido não encontrado para payment {}", paymentResponse.getId())
                    );
        } catch (PaymentException exception) {
            log.error("[pix] Erro ao processar webhook do Mercado Pago", exception);
        }
    }

    private void updateOrderFromPayment(Order order, MercadoPagoPaymentResponse paymentResponse) {
        String normalizedStatus = normalizeStatus(paymentResponse.getStatus());
        order.setStatusOrder(normalizedStatus);
        order.setPickupQrToken(extractQrCode(paymentResponse));
        order.setPixQrCodeBase64(extractQrCodeBase64(paymentResponse));
        order.setPixExpiresAt(parseExpiration(paymentResponse.getDateOfExpiration()));
        orderRepository.save(order);
    }

    private String normalizeStatus(String mercadoPagoStatus) {
        if (!StringUtils.hasText(mercadoPagoStatus)) {
            return "pending";
        }

        return switch (mercadoPagoStatus.toLowerCase()) {
            case "approved" -> "paid";
            case "rejected", "cancelled", "charged_back" -> "canceled";
            default -> "pending";
        };
    }

    private String extractQrCode(MercadoPagoPaymentResponse response) {
        return Optional.ofNullable(response.getPointOfInteraction())
                .map(MercadoPagoPaymentResponse.PointOfInteraction::getTransactionData)
                .map(MercadoPagoPaymentResponse.TransactionData::getQrCode)
                .orElse(null);
    }

    private String extractQrCodeBase64(MercadoPagoPaymentResponse response) {
        return Optional.ofNullable(response.getPointOfInteraction())
                .map(MercadoPagoPaymentResponse.PointOfInteraction::getTransactionData)
                .map(MercadoPagoPaymentResponse.TransactionData::getQrCodeBase64)
                .orElse(null);
    }

    private OffsetDateTime parseExpiration(String rawExpiration) {
        if (!StringUtils.hasText(rawExpiration)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(rawExpiration);
        } catch (DateTimeParseException exception) {
            log.warn("[pix] Não foi possível converter a data de expiração: {}", rawExpiration);
            return null;
        }
    }

    private String buildCheckoutReference() {
        return "PIX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
