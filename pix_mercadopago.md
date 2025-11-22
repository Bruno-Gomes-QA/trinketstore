
# Integração PIX com Mercado Pago (Java 21 + Spring Boot + Gradle)

## 1. build.gradle (Groovy) com Java 21

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.3'
    id 'io.spring.dependency-management' version '1.1.5'
}

group = 'com.exemplo'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

---

## 2. application.properties

```properties
server.port=8080

mercadopago.access-token=SEU_ACCESS_TOKEN_AQUI
app.base-url=https://sua-url-publica.com
```

---

## 3. Classe Principal

```java
package com.exemplo.pix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class PixApplication {

    public static void main(String[] args) {
        SpringApplication.run(PixApplication.class, args);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

---

## 4. DTOs

### CreatePixRequest

```java
package com.exemplo.pix.dto;

import lombok.Data;

@Data
public class CreatePixRequest {
    private Double value;
    private String description;
    private String externalReference;
}
```

### CreatePixResponse

```java
package com.exemplo.pix.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePixResponse {
    private Long id;
    private String status;
    private String qrCode;
    private String qrCodeBase64;
    private String ticketUrl;
    private String externalReference;
}
```

---

## 5. Serviço MercadoPagoService

```java
package com.exemplo.pix.service;

import com.exemplo.pix.dto.CreatePixRequest;
import com.exemplo.pix.dto.CreatePixResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${app.base-url}")
    private String baseUrl;

    private final WebClient.Builder webClientBuilder;

    private WebClient client() {
        return webClientBuilder
                .baseUrl("https://api.mercadopago.com")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    public CreatePixResponse createPixPayment(CreatePixRequest req) {
        Map<String, Object> body = new HashMap<>();
        body.put("transaction_amount", req.getValue());
        body.put("description",
                req.getDescription() != null ? req.getDescription() : "Pagamento via Pix");
        body.put("payment_method_id", "pix");
        body.put("external_reference",
                req.getExternalReference() != null
                        ? req.getExternalReference()
                        : "ref-" + System.currentTimeMillis());
        body.put("notification_url", baseUrl + "/webhook/mercadopago");

        Map<String, Object> payer = new HashMap<>();
        payer.put("email", "cliente@example.com");
        body.put("payer", payer);

        Map response = client()
                .post()
                .uri("/v1/payments")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            throw new IllegalStateException("Resposta nula da API do Mercado Pago");
        }

        Long id = ((Number) response.get("id")).longValue();
        String status = (String) response.get("status");
        String externalReference = (String) response.get("external_reference");

        Map<String, Object> poi = (Map<String, Object>) response.get("point_of_interaction");
        Map<String, Object> transactionData =
                poi != null ? (Map<String, Object>) poi.get("transaction_data") : null;

        String qrCode = transactionData != null ? (String) transactionData.get("qr_code") : null;
        String qrCodeBase64 = transactionData != null ? (String) transactionData.get("qr_code_base64") : null;
        String ticketUrl = transactionData != null ? (String) transactionData.get("ticket_url") : null;

        return CreatePixResponse.builder()
                .id(id)
                .status(status)
                .externalReference(externalReference)
                .qrCode(qrCode)
                .qrCodeBase64(qrCodeBase64)
                .ticketUrl(ticketUrl)
                .build();
    }

    public Map<String, Object> getPaymentById(String id) {
        Map<String, Object> payment = client()
                .get()
                .uri("/v1/payments/{id}", id)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (payment == null) {
            throw new IllegalStateException("Pagamento não encontrado na API do Mercado Pago");
        }

        return payment;
    }
}
```

---

## 6. Controller PixController

```java
package com.exemplo.pix.controller;

import com.exemplo.pix.dto.CreatePixRequest;
import com.exemplo.pix.dto.CreatePixResponse;
import com.exemplo.pix.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PixController {

    private final MercadoPagoService mercadoPagoService;

    @PostMapping("/pix/create")
    public ResponseEntity<CreatePixResponse> createPix(@RequestBody CreatePixRequest request) {
        try {
            CreatePixResponse resp = mercadoPagoService.createPixPayment(request);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Erro ao criar pagamento Pix", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> body) {
        try {
            log.info("Webhook recebido: {}", body);

            String type = (String) body.get("type");
            Map<String, Object> data = (Map<String, Object>) body.get("data");

            if ("payment".equalsIgnoreCase(type) && data != null && data.get("id") != null) {
                String paymentId = String.valueOf(data.get("id"));
                Map<String, Object> payment = mercadoPagoService.getPaymentById(paymentId);

                String status = (String) payment.get("status");
                String externalReference = (String) payment.get("external_reference");

                log.info("Pagamento atualizado. id={}, status={}, externalReference={}",
                        paymentId, status, externalReference);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao processar webhook", e);
            return ResponseEntity.ok().build();
        }
    }
}
```

---

## 7. Resumo do Fluxo

1. Front chama `POST /pix/create`
2. Backend cria pagamento Pix e devolve:
   - qrCode  
   - qrCodeBase64  
   - ticketUrl  
3. Cliente paga  
4. Mercado Pago envia `POST /webhook/mercadopago`  
5. Backend verifica o status (`approved`) e processa o pedido  

---

## Arquivo completo gerado automaticamente

```
pix_mercadopago.md
```
