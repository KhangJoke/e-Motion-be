package com.swp391.e_Motion_be.config;

import com.swp391.e_Motion_be.entity.Payment;
import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.service.PaymentService;
import com.swp391.e_Motion_be.service.ReservationService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Bật keyspace notifications
        enableKeyspaceNotifications(connectionFactory);

        return template;
    }

    private void enableKeyspaceNotifications(RedisConnectionFactory connectionFactory) {
        try {
            RedisConnection connection = connectionFactory.getConnection();
            connection.setConfig("notify-keyspace-events", "Ex");
            connection.close();
            log.info("Redis keyspace notifications enabled");
        } catch (Exception e) {
            log.error("Failed to enable Redis keyspace notifications", e);
        }
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListener(
            RedisConnectionFactory connectionFactory,
            RedisKeyExpirationListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new PatternTopic("__keyevent@0__:expired"));
        log.info("Redis message listener container configured");
        return container;
    }

    @Component
    public static class RedisKeyExpirationListener implements MessageListener {

        private final ReservationService reservationService;
        private final PaymentService paymentService;

        public RedisKeyExpirationListener(PaymentService paymentService, ReservationService reservationService) {
            this.reservationService = reservationService;
            this.paymentService = paymentService;
        }

        @Transactional
        @Override
        public void onMessage(Message message, byte[] pattern) {
            String expiredKey = new String(message.getBody());
            log.warn("Expired key detected: {}", expiredKey);

            if (expiredKey.startsWith("reservation:")) {
                try {
                    long id = Long.parseLong(expiredKey.split(":")[1]);
                    log.info("Processing expired reservation: {}", id);

                    Reservation reservation = reservationService.getById(id);
                    if (reservation != null && "PENDING".equals(reservation.getStatus().toString())) {
                        Payment payment = paymentService.getPaymentByReservationId(reservation.getId());
                        paymentService.processFailedPayment(payment);
                        log.info("Handled expired reservation: {}", id);
                    } else {
                        log.info("No action needed for reservation: {}", id);
                    }
                } catch (Exception e) {
                    log.error("Error processing expired key: {}", expiredKey, e);
                }
            }
        }
    }
}


