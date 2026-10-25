package com.swp391.e_Motion_be.rabbitmq.producer;

import com.swp391.e_Motion_be.config.RabbitMQConfig;
import com.swp391.e_Motion_be.dto.message.RentalExtendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendRentalExtendEvent(RentalExtendMessage message) {
        try {
            log.info("Sending rental extension notification event to RabbitMQ: rentalId={}, newEndTime={}",
                    message.getRentalId(), message.getNewEndTime());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RENTAL_EXTEND_EXCHANGE,
                    RabbitMQConfig.RENTAL_EXTEND_ROUTING_KEY,
                    message
            );
            log.info("Rental extension event sent successfully to RabbitMQ for rentalId={}", message.getRentalId());
        } catch (Exception e) {
            log.warn("Failed to send rental extension event to RabbitMQ (non-blocking): {}", e.getMessage());
        }
    }
}
