package message.agendamentosala.infrastructure.gateway.messaging;

import lombok.AllArgsConstructor;
import message.agendamentosala.domain.model.Reservation;
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

    public void sendStandByTimeout(Reservation reservation) {
        Long reservationId = reservation.id();

        System.out.println("PT: PRODUCER - Enviando ID " + reservationId + " para a fila STAND_BY_DELAY.");

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STAND_BY_DELAY_EXCHANGE,
                RabbitMQConfig.STAND_BY_DELAY_ROUTING_KEY,
                reservationId
        );
    }

    public void sendCheckInConfirmation(Long reservationId) {
        System.out.println("PT: PRODUCER - Enviando ID " + reservationId + " para a fila CHECK_IN_QUEUE.");

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHECK_IN_EXCHANGE,
                RabbitMQConfig.CHECK_IN_ROUTING_KEY,
                reservationId
        );
    }
}