package message.agendamentosala.infrastructure.messaging;

import lombok.AllArgsConstructor;
import message.agendamentosala.application.usecase.user.CreateUserUseCase;
import message.agendamentosala.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CancellationConsumer {

    private final CreateUserUseCase cancellationUseCase;

    @RabbitListener(queues = RabbitMQConfig.CANCELLATION_QUEUE)
    public void consumeCancellation(Long reservationId) {
        System.out.println("--- CONSUMER (Cancellation) ---");
        System.out.println("PT: Mensagem de Cancelamento (DLX) recebida. ID da Reserva: " + reservationId);
        System.out.println("EN: Cancellation message (DLX) received. Reservation ID: " + reservationId);

        try {
            var feedback = cancellationUseCase.execute(reservationId);

            System.out.println("PT: Processamento concluído: " + feedback);
            System.out.println("EN: Processing complete: " + feedback);

        } catch (Exception e) {
            System.err.println("PT: Falha ao processar Cancelamento para ID " + reservationId + ". Erro: " + e.getMessage());
            System.err.println("EN: Failed to process Cancellation for ID " + reservationId + ". Error: " + e.getMessage());
        }
    }
}
