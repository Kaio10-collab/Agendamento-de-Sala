package message.agendamentosala.infrastructure.messaging;

import lombok.AllArgsConstructor;
import message.agendamentosala.application.usecase.ProcessCheckInUseCase;
import message.agendamentosala.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CheckInConfirmationConsumer {

    private final ProcessCheckInUseCase checkInUseCase;

    @RabbitListener(queues = RabbitMQConfig.CHECK_IN_ROUTING_KEY)
    public void consumeCheckInConfirmation(Long reservationId) {
        System.out.println("--- CONSUMER (Check-in Confirmation) ---");
        System.out.println("PT: Mensagem recebida para Check-in. ID da Reserva: " + reservationId);
        System.out.println("EN: Message received for Check-in. Reservation ID: " + reservationId);

        try {
            var feedback = checkInUseCase.execute(reservationId);

            System.out.println("PT: Processamento concluído: " + feedback);
            System.out.println("EN: Processing complete: " + feedback);

        } catch (Exception e) {
            System.err.println("PT: Falha ao processar Check-in para ID " + reservationId + ". Erro: " + e.getMessage());
            System.err.println("EN: Failed to process Check-in for ID " + reservationId + ". Error: " + e.getMessage());
        }
    }
}
