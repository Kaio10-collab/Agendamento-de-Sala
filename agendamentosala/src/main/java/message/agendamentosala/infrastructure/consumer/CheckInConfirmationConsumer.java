package message.agendamentosala.infrastructure.consumer;

import lombok.AllArgsConstructor;
import message.agendamentosala.application.usecase.reservation.ProcessCheckInUseCase;
import message.agendamentosala.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CheckInConfirmationConsumer {

    private final ProcessCheckInUseCase checkInUseCase;

    @RabbitListener(queues = RabbitMQConfig.CHECK_IN_QUEUE)
    public void receiveCheckInMessage(Long reservationId) {
        try {
            System.out.println("CONSUMER - Recebeu mensagem de Check-in para ID: " + reservationId);
            checkInUseCase.execute(reservationId);
        } catch (Exception e) {
            System.err.println("Erro ao processar check-in para ID " + reservationId + ": " + e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Erro ao processar o check-in");
        }
    }
}