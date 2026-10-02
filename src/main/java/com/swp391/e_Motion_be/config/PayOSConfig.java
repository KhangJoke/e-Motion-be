package com.swp391.e_Motion_be.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Slf4j
@Getter
@Configuration
public class PayOSConfig {

    @Value("${payos.client-id:787400f4-547a-4ded-8a16-bb6ff325087f}")
    private String clientId;

    @Value("${payos.api-key:9ed9a0e1-daf4-4a3a-b414-4993c61fedda}")
    private String apiKey;

    @Value("${payos.checksum-key:fcecbcbd798b8f2da9fed33e0792ecedd414f77bec88f0f981528ef2f275a45e}")
    private String checksumKey;

    @Value("${payos.return-url:https://e-motion-fe.vercel.app/payments/payment-result}")
    private String returnUrl;

    @Value("${payos.cancel-url:https://e-motion-fe.vercel.app/payments/payment-result?status=failed}")
    private String cancelUrl;

    @Bean
    public PayOS payOS() {
        if (isConfigured()) {
            log.info("PayOS initialized successfully with client-id: {}", clientId);
            return new PayOS(clientId.trim(), apiKey.trim(), checksumKey.trim());
        }
        log.warn("PayOS credentials are empty. PayOS initialized in inactive/fallback mode until valid keys are configured in application.properties or .env");
        return null;
    }

    public boolean isConfigured() {
        return clientId != null && !clientId.trim().isEmpty()
                && apiKey != null && !apiKey.trim().isEmpty()
                && checksumKey != null && !checksumKey.trim().isEmpty();
    }
}
