package com.reducess.trinketstore.service;

import com.reducess.trinketstore.dto.MercadoPagoPaymentResponse;
import com.reducess.trinketstore.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoClient {

    @Value("${mercadopago.base-url:https://api.mercadopago.com}")
    private String baseUrl;

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    private final WebClient.Builder webClientBuilder;

    private WebClient client() {
        ensureAccessToken();
        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Cria um pagamento PIX no Mercado Pago.
     *
     * @param value             valor em BRL (ex: 11.40)
     * @param description       descrição do pedido
     * @param externalReference referência única do seu sistema (pedido, carrinho etc.)
     * @param payerEmail        e-mail do pagador (opcional)
     * @param payerFirstName    nome do pagador (opcional)
     * @param payerLastName     sobrenome do pagador (opcional)
     * @param payerDocumentType tipo do documento (ex: CPF)
     * @param payerDocument     número do documento
     */
    public MercadoPagoPaymentResponse createPixPayment(
            BigDecimal value,
            String description,
            String externalReference,
            String payerEmail,
            String payerFirstName,
            String payerLastName,
            String payerDocumentType,
            String payerDocument
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("transaction_amount", value);
        body.put("description",
                StringUtils.hasText(description) ? description : "Pagamento via Pix");
        body.put("payment_method_id", "pix");
        body.put("external_reference",
                StringUtils.hasText(externalReference)
                        ? externalReference
                        : "ref-" + System.currentTimeMillis());

        Map<String, Object> payer = new HashMap<>();
        payer.put("email",
                StringUtils.hasText(payerEmail) ? payerEmail : "cliente@example.com");
        payer.put("first_name", StringUtils.hasText(payerFirstName) ? payerFirstName : "Test");
        payer.put("last_name", StringUtils.hasText(payerLastName) ? payerLastName : "User");

        Map<String, Object> identification = new HashMap<>();
        identification.put("type", StringUtils.hasText(payerDocumentType) ? payerDocumentType : "CPF");
        identification.put("number", StringUtils.hasText(payerDocument) ? payerDocument : "19119119100");
        payer.put("identification", identification);

        body.put("payer", payer);

        // Gera uma chave de idempotência única por tentativa
        String idempotencyKey = UUID.randomUUID().toString();
        log.info("Criando pagamento PIX no Mercado Pago com X-Idempotency-Key={}", idempotencyKey);
        log.debug("Payload enviado ao Mercado Pago: {}", body);

        MercadoPagoPaymentResponse response = client()
                .post()
                .uri("/v1/payments")
                .header("X-Idempotency-Key", idempotencyKey) // 👈 obrigatório agora
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, httpResponse ->
                        httpResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(message -> {
                                    log.error("Erro ao criar pagamento PIX no Mercado Pago: {}", message);
                                    return new PaymentException("Falha ao criar pagamento PIX: " + message);
                                })
                )
                .bodyToMono(MercadoPagoPaymentResponse.class)
                .block();

        if (response == null || response.getId() == null) {
            throw new PaymentException("Resposta vazia do Mercado Pago ao criar o pagamento PIX.");
        }

        log.info("Pagamento PIX criado com sucesso no Mercado Pago. id={}, status={}",
                response.getId(), response.getStatus());

        return response;
    }

    /**
     * Consulta um pagamento PIX no Mercado Pago pelo ID.
     */
    public MercadoPagoPaymentResponse getPayment(String paymentId) {
        log.info("Consultando pagamento PIX no Mercado Pago. id={}", paymentId);

        MercadoPagoPaymentResponse response = client()
                .get()
                .uri("/v1/payments/{id}", paymentId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, httpResponse ->
                        httpResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(message -> {
                                    log.error("Erro ao consultar pagamento PIX no Mercado Pago: {}", message);
                                    return new PaymentException("Não foi possível consultar o pagamento PIX: " + message);
                                })
                )
                .bodyToMono(MercadoPagoPaymentResponse.class)
                .block();

        if (response == null || response.getId() == null) {
            throw new PaymentException("Pagamento PIX não encontrado no Mercado Pago.");
        }

        log.info("Pagamento PIX consultado com sucesso. id={}, status={}",
                response.getId(), response.getStatus());

        return response;
    }

    /**
     * Garante que o access token foi configurado.
     */
    private void ensureAccessToken() {
        if (!StringUtils.hasText(accessToken)) {
            throw new PaymentException("Configure o token do Mercado Pago para habilitar o PIX.");
        }
    }
}
