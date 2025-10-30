package message.agendamentosala.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String SCHEDULING_EXCHANGE = "room.scheduling.exchange";
    public static final String CHECK_IN_ROUTING_KEY = "room.scheduling.checkin";
    public static final String CANCELLATION_QUEUE = "room.scheduling.cancellation";
    public static final String DELAY_EXCHANGE = "room.scheduling.delay.exchange";
    public static final String DELAY_QUEUE = "room.scheduling.delay.queue";

    @Bean
    public DirectExchange schedulingExchange() {
        return new DirectExchange(SCHEDULING_EXCHANGE);
    }

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(DELAY_EXCHANGE);
    }

    @Bean
    public Queue checkInQueue() {
        return new Queue(CHECK_IN_ROUTING_KEY, true);
    }

    @Bean
    public Queue cancellationQueue() {
        return new Queue(CANCELLATION_QUEUE, true);
    }

    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", SCHEDULING_EXCHANGE);
        args.put("x-dead-letter-routing-key", CHECK_IN_ROUTING_KEY);

        return QueueBuilder.durable(DELAY_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding checkInBinding() {
        return BindingBuilder.bind(checkInQueue())
                .to(schedulingExchange())
                .with(CHECK_IN_ROUTING_KEY);
    }

    @Bean
    public Binding cancellationBinding() {
        return BindingBuilder.bind(cancellationQueue())
                .to(schedulingExchange())
                .with(CANCELLATION_QUEUE);
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue())
                .to(delayExchange())
                .with(CANCELLATION_QUEUE);
    }
}
