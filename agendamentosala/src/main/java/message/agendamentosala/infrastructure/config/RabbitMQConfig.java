package message.agendamentosala.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String STAND_BY_DELAY_ROUTING_KEY = "standby.delay";
    public static final String STAND_BY_CANCEL_ROUTING_KEY = "standby.cancel";
    public static final String STAND_BY_DELAY_EXCHANGE = "room.standby.delay.exchange";
    public static final String STAND_BY_DELAY_QUEUE = "room.standby.delay.queue";
    public static final String STAND_BY_DLX_EXCHANGE = "room.standby.dlx.exchange";
    public static final String STAND_BY_CANCEL_QUEUE = "room.standby.cancel.queue";
    public static final String CHECK_IN_ROUTING_KEY = "checkin.confirm";
    public static final String CHECK_IN_EXCHANGE = "room.checkin.exchange";
    public static final String CHECK_IN_QUEUE = "room.checkin.queue";
    public static final long STAND_BY_TTL_MS = 15 * 60 * 1000;

    // --- 1. Exchanges ---

    @Bean
    public Exchange standbyDelayExchange() {
        return ExchangeBuilder.directExchange(STAND_BY_DELAY_EXCHANGE).build();
    }

    @Bean
    public Exchange standbyDlxExchange() {
        return ExchangeBuilder.directExchange(STAND_BY_DLX_EXCHANGE).build();
    }

    @Bean
    public Exchange checkInExchange() {
        return ExchangeBuilder.directExchange(CHECK_IN_EXCHANGE).build();
    }

    // --- 2. Filas ---

    @Bean
    public Queue standbyDelayQueue() {
        return QueueBuilder.durable(STAND_BY_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", STAND_BY_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", STAND_BY_CANCEL_ROUTING_KEY)
                .withArgument("x-message-ttl", STAND_BY_TTL_MS)
                .build();
    }

    @Bean
    public Queue standbyCancelQueue() {
        return QueueBuilder.durable(STAND_BY_CANCEL_QUEUE).build();
    }

    @Bean
    public Queue checkInQueue() {
        return QueueBuilder.durable(CHECK_IN_QUEUE).build();
    }

    // --- 3. Bindings ---

    @Bean
    public Binding standbyDelayBinding() {
        return BindingBuilder.bind(standbyDelayQueue())
                .to(standbyDelayExchange())
                .with(STAND_BY_DELAY_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding standbyCancelBinding() {
        return BindingBuilder.bind(standbyCancelQueue())
                .to(standbyDlxExchange())
                .with(STAND_BY_CANCEL_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding checkInBinding() {
        return BindingBuilder.bind(checkInQueue())
                .to(checkInExchange())
                .with(CHECK_IN_ROUTING_KEY).noargs();
    }
}