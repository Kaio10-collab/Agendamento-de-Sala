package message.agendamentosala.infrastructure.gateway.messaging;

import lombok.AllArgsConstructor;
import message.agendamentosala.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class ReservationProducerGateway {

    private final RabbitTemplate rabbitTemplate;

    public void sendCancellationDelay(Reservation reservation) {

        var checkInDeadline = reservation.startDateTime().plusMinutes(15);
        long delayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), checkInDeadline);

        if (delayMillis < 0) {
            delayMillis = 1;
        }

        var reservationId = reservation.id();

        var finalDelayMillis = delayMillis;
        MessagePostProcessor ttlProcessor = message -> {
            message.getMessageProperties().setExpiration(String.valueOf(finalDelayMillis));
            return message;
        };

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELAY_EXCHANGE,
                    RabbitMQConfig.CANCELLATION_QUEUE,
                    reservationId,
                    ttlProcessor
            );
        } catch (AmqpException e) {
            System.err.println("Failed to send cancellation delay message for ID: " + reservationId + ". Error: " + e.getMessage());
        }
    }

    public void sendCheckInConfirmation(Long reservationId) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SCHEDULING_EXCHANGE,
                    RabbitMQConfig.CHECK_IN_ROUTING_KEY,
                    reservationId
            );
        } catch (AmqpException e) {
            System.err.println("Failed to send check-in confirmation message for ID: " + reservationId + ". Error: " + e.getMessage());
        }
    }
}
