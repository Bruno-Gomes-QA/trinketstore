package com.reducess.trinketstore.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(PaymentClient.class)
@ConditionalOnProperty(prefix = "mercadopago", name = "access-token", matchIfMissing = false)
public class MercadoPagoConfiguration {

    @Bean
    public PaymentClient paymentClient(@Value("${mercadopago.access-token}") String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new IllegalStateException("Mercado Pago access token must be configured");
        }
        MercadoPagoConfig.setAccessToken(accessToken);
        return new PaymentClient();
    }
}
