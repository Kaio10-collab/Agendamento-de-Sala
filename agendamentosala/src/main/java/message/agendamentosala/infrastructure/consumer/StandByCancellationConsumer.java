package message.agendamentosala.infrastructure.consumer;

import lombok.AllArgsConstructor;
import message.agendamentosala.application.usecase.reservation.ProcessStandByCancellationUseCase;
import message.agendamentosala.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StandByCancellationConsumer {

    private final ProcessStandByCancellationUseCase cancellationUseCase;

    @RabbitListener(queues = RabbitMQConfig.STAND_BY_CANCEL_QUEUE)
    public void receiveCancellationMessage(Long reservationId) {
        try {
            System.out.println("CONSUMER - Recebeu mensagem de expiração para ID: " + reservationId);
            cancellationUseCase.execute(reservationId);
        } catch (Exception e) {
            System.err.println("Erro ao processar cancelamento de Stand By para ID " + reservationId + ": " + e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Erro ao processar o cancelamento do Stand By");
        }
    }
}