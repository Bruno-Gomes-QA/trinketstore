package com.reducess.trinketstore.service;

import com.reducess.trinketstore.dto.CreateOrderRequest;
import com.reducess.trinketstore.dto.MercadoPagoPaymentResponse;
import com.reducess.trinketstore.dto.OrderResponse;
import com.reducess.trinketstore.dto.PixCheckoutRequest;
import com.reducess.trinketstore.dto.PixCheckoutResponse;
import com.reducess.trinketstore.dto.PixPaymentDetails;
import com.reducess.trinketstore.entity.Order;
import com.reducess.trinketstore.entity.User;
import com.reducess.trinketstore.repository.OrderRepository;
import com.reducess.trinketstore.repository.UserRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PixPaymentService {

    private static final String DEFAULT_PAYER_FIRST_NAME = "Test";
    private static final String DEFAULT_PAYER_LAST_NAME = "User";
    private static final String DEFAULT_DOCUMENT_TYPE = "CPF";
    private static final String DEFAULT_DOCUMENT_NUMBER = "19119119100";
    private static final Set<String> FINAL_MP_STATUSES = Set.of(
            "approved", "authorized", "rejected", "cancelled", "canceled", "charged_back", "refunded"
    );

    @Value("${pix.polling.interval-seconds:10}")
    private long pollingIntervalSeconds;

    @Value("${pix.polling.timeout-seconds:300}")
    private long pollingTimeoutSeconds;

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MercadoPagoClient mercadoPagoClient;
    private final InventoryService inventoryService;
    private final PlatformTransactionManager transactionManager;

    private final ScheduledExecutorService pixScheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, ScheduledFuture<?>> activePollers = new ConcurrentHashMap<>();

    @Transactional
    public PixCheckoutResponse createPixCheckout(PixCheckoutRequest request) {
        User user = userRepository.findById(request.getUserId().longValue())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String checkoutId = buildCheckoutReference();
        BigDecimal amount = BigDecimal.valueOf(request.getTotalAmountInCents())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        NameParts payerName = splitName(user.getNomeUser());

        inventoryService.verifyStockAvailability(request.getItems());
        MercadoPagoPaymentResponse paymentResponse = mercadoPagoClient.createPixPayment(
                amount,
                request.getDescription(),
                checkoutId,
                user.getEmail(),
                payerName.firstName(),
                payerName.lastName(),
                DEFAULT_DOCUMENT_TYPE,
                DEFAULT_DOCUMENT_NUMBER
        );
        String paymentId = String.valueOf(paymentResponse.getId());
        log.info("[pix] Pagamento {} criado no Mercado Pago. Iniciando polling a cada {}s.", paymentId, pollingIntervalSeconds);

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(request.getUserId());
        orderRequest.setStatusOrder("pending");
        orderRequest.setTotalOrders(request.getTotalAmountInCents());
        orderRequest.setCurrencyOrder(StringUtils.hasText(request.getCurrency()) ? request.getCurrency() : "BRL");
        orderRequest.setCheckoutId(checkoutId);
        orderRequest.setPaymentIntent(paymentId);
        orderRequest.setPickupQrToken(extractQrCode(paymentResponse));
        orderRequest.setPixQrCodeBase64(extractQrCodeBase64(paymentResponse));
        orderRequest.setPixExpiresAt(parseExpiration(paymentResponse.getDateOfExpiration()));
        orderRequest.setItems(request.getItems());

        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        PixPaymentDetails paymentDetails = new PixPaymentDetails(
                paymentId,
                paymentResponse.getStatus(),
                orderResponse.getPickupQrToken(),
                orderResponse.getPixQrCodeBase64(),
                orderResponse.getPixExpiresAt(),
                orderResponse.getCheckoutId()
        );

        startStatusPolling(paymentId);

        return new PixCheckoutResponse(orderResponse, paymentDetails);
    }

    @Transactional
    public PixCheckoutResponse refreshPixPayment(String paymentId) {
        MercadoPagoPaymentResponse paymentResponse = mercadoPagoClient.getPayment(paymentId);

        Order order = orderRepository.findByPaymentIntent(String.valueOf(paymentResponse.getId()))
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado para payment " + paymentId));

        Order savedOrder = updateOrderFromPayment(order, paymentResponse);
        if (isFinalStatus(paymentResponse.getStatus())) {
            stopPolling(String.valueOf(paymentResponse.getId()));
        }

        OrderResponse orderResponse = orderService.getOrderById(savedOrder.getIdOrder());
        PixPaymentDetails paymentDetails = new PixPaymentDetails(
                String.valueOf(paymentResponse.getId()),
                paymentResponse.getStatus(),
                orderResponse.getPickupQrToken(),
                orderResponse.getPixQrCodeBase64(),
                orderResponse.getPixExpiresAt(),
                orderResponse.getCheckoutId()
        );

        return new PixCheckoutResponse(orderResponse, paymentDetails);
    }

    private void startStatusPolling(String paymentId) {
        activePollers.computeIfAbsent(paymentId, id -> scheduleNextPoll(id, Instant.now()));
    }

    private ScheduledFuture<?> scheduleNextPoll(String paymentId, Instant startedAt) {
        return pixScheduler.schedule(() -> pollPaymentStatus(paymentId, startedAt),
                pollingIntervalSeconds, TimeUnit.SECONDS);
    }

    private void pollPaymentStatus(String paymentId, Instant startedAt) {
        boolean continuePolling = true;
        try {
            MercadoPagoPaymentResponse paymentResponse = mercadoPagoClient.getPayment(paymentId);
            log.info("[pix] Polling {} -> status={} detail={}",
                    paymentId,
                    paymentResponse.getStatus(),
                    paymentResponse.getStatusDetail());

            runInTransaction(() -> orderRepository.findByPaymentIntent(String.valueOf(paymentResponse.getId()))
                    .ifPresent(order -> updateOrderFromPayment(order, paymentResponse)));

            if (isFinalStatus(paymentResponse.getStatus())) {
                continuePolling = false;
            }

            if (timedOut(startedAt)) {
                log.warn("[pix] Polling encerrado por timeout para pagamento {}", paymentId);
                continuePolling = false;
            }
        } catch (Exception exception) {
            log.warn("[pix] Falha ao consultar status do pagamento {}: {}", paymentId, exception.getMessage());
        }

        if (continuePolling) {
            activePollers.put(paymentId, scheduleNextPoll(paymentId, startedAt));
        } else {
            stopPolling(paymentId);
        }
    }

    private boolean timedOut(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).getSeconds() >= pollingTimeoutSeconds;
    }

    private void stopPolling(String paymentId) {
        Optional.ofNullable(activePollers.remove(paymentId))
                .ifPresent(future -> future.cancel(false));
    }

    private void runInTransaction(Runnable action) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> action.run());
    }

    private boolean isFinalStatus(String mercadoPagoStatus) {
        if (!StringUtils.hasText(mercadoPagoStatus)) {
            return false;
        }
        return FINAL_MP_STATUSES.contains(mercadoPagoStatus.toLowerCase());
    }

    private Order updateOrderFromPayment(Order order, MercadoPagoPaymentResponse paymentResponse) {
        String normalizedStatus = normalizeStatus(paymentResponse.getStatus());
        order.setStatusOrder(normalizedStatus);
        order.setPickupQrToken(extractQrCode(paymentResponse));
        order.setPixQrCodeBase64(extractQrCodeBase64(paymentResponse));
        order.setPixExpiresAt(parseExpiration(paymentResponse.getDateOfExpiration()));
        return orderRepository.save(order);
    }

    private String normalizeStatus(String mercadoPagoStatus) {
        if (!StringUtils.hasText(mercadoPagoStatus)) {
            return "pending";
        }

        return switch (mercadoPagoStatus.toLowerCase()) {
            case "approved", "authorized" -> "paid";
            case "rejected", "cancelled", "canceled", "charged_back", "refunded" -> "canceled";
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

    private NameParts splitName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return new NameParts(DEFAULT_PAYER_FIRST_NAME, DEFAULT_PAYER_LAST_NAME);
        }

        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0];
        String last = parts.length > 1
                ? String.join(" ", Arrays.copyOfRange(parts, 1, parts.length))
                : DEFAULT_PAYER_LAST_NAME;
        return new NameParts(first, last);
    }

    private record NameParts(String firstName, String lastName) {
    }

    private String buildCheckoutReference() {
        return "PIX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    @PreDestroy
    public void shutdownScheduler() {
        pixScheduler.shutdownNow();
    }
}
