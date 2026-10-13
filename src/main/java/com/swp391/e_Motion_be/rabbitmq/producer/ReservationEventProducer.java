package com.swp391.e_Motion_be.rabbitmq.producer;

import com.swp391.e_Motion_be.config.RabbitMQConfig;
import com.swp391.e_Motion_be.dto.message.ReservationTimeoutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendReservationHoldTimeoutEvent(Long reservationId, String reservationCode) {
        try {
            ReservationTimeoutMessage message = ReservationTimeoutMessage.builder()
                    .reservationId(reservationId)
                    .reservationCode(reservationCode)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.info("Sending reservation timeout event to RabbitMQ delay queue: id={}, code={}", reservationId, reservationCode);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.HOLD_EXCHANGE,
                    RabbitMQConfig.HOLD_ROUTING_KEY,
                    message
            );
            log.info("Reservation timeout event sent successfully to RabbitMQ: id={}", reservationId);
        } catch (Exception e) {
            log.error("Failed to send reservation timeout event to RabbitMQ for reservation id={}: {}", reservationId, e.getMessage(), e);
        }
    }
}
