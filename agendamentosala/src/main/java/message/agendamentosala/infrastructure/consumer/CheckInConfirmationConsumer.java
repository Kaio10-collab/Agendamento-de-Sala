package message.agendamentosala.infrastructure.consumer;

import lombok.AllArgsConstructor;
import message.agendamentosala.application.usecase.user.ReadUserUseCase;
import message.agendamentosala.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CheckInConfirmationConsumer {

    private final ReadUserUseCase checkInUseCase;

    @RabbitListener(queues = RabbitMQConfig.CHECK_IN_ROUTING_KEY)
    public void consumeCheckInConfirmation(String reservationId) {
        System.out.println("--- CONSUMER (Check-in Confirmation) ---");
        System.out.println("PT: Mensagem recebida para Check-in. ID da Reserva: " + reservationId);

        try {
            var feedback = checkInUseCase.execute(Long.valueOf(reservationId));
            System.out.println("PT: Processamento concluído: " + feedback);

        } catch (Exception e) {
            System.err.println("PT: Falha ao processar Check-in para ID " + reservationId + ". Erro: " + e.getMessage());
        }
    }
}
