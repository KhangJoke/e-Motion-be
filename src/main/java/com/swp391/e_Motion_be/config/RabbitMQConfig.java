package com.swp391.e_Motion_be.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String HOLD_EXCHANGE = "reservation.hold.exchange";
    public static final String HOLD_ROUTING_KEY = "reservation.hold.routing";
    public static final String DELAY_QUEUE = "reservation.hold.delay.queue";

    public static final String CANCEL_EXCHANGE = "reservation.cancel.exchange";
    public static final String CANCEL_ROUTING_KEY = "reservation.cancel.routing";
    public static final String CANCEL_QUEUE = "reservation.cancel.process.queue";

    @Value("${reservation.hold.timeout-ms:900000}")
    private long holdTimeoutMs;

    @Bean
    public DirectExchange reservationHoldExchange() {
        return new DirectExchange(HOLD_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange reservationCancelExchange() {
        return new DirectExchange(CANCEL_EXCHANGE, true, false);
    }

    @Bean
    public Queue reservationHoldDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", holdTimeoutMs);
        args.put("x-dead-letter-exchange", CANCEL_EXCHANGE);
        args.put("x-dead-letter-routing-key", CANCEL_ROUTING_KEY);
        return new Queue(DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue reservationCancelProcessQueue() {
        return new Queue(CANCEL_QUEUE, true);
    }

    @Bean
    public Binding bindingHoldDelayQueue() {
        return BindingBuilder.bind(reservationHoldDelayQueue())
                .to(reservationHoldExchange())
                .with(HOLD_ROUTING_KEY);
    }

    public static final String RENTAL_EXTEND_EXCHANGE = "rental.extend.exchange";
    public static final String RENTAL_EXTEND_ROUTING_KEY = "rental.extend.routing";
    public static final String RENTAL_EXTEND_QUEUE = "rental.extend.notification.queue";

    @Bean
    public DirectExchange rentalExtendExchange() {
        return new DirectExchange(RENTAL_EXTEND_EXCHANGE, true, false);
    }

    @Bean
    public Queue rentalExtendNotificationQueue() {
        return new Queue(RENTAL_EXTEND_QUEUE, true);
    }

    @Bean
    public Binding bindingRentalExtendQueue() {
        return BindingBuilder.bind(rentalExtendNotificationQueue())
                .to(rentalExtendExchange())
                .with(RENTAL_EXTEND_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
