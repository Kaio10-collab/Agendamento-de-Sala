package message.agendamentosala.application.usecase.reservation;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.validator.EmailValidator;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteReservationUseCase {

    private final ReservationPersistenceGateway persistenceGateway;

    @Transactional
    public void execute(String email) {
        EmailValidator.validate(email);

        Reservation activeReservation = persistenceGateway.findActiveReservationsByUserEmail(email).stream()
                .findFirst()
                .orElseThrow(() -> new ValidationException("Não é possível excluir: Nenhuma reserva ativa encontrada para o e-mail: " + email));

        persistenceGateway.deleteById(activeReservation.id());
        System.out.println("Id da Reserva " + activeReservation.id() + " excluído com sucesso.");
    }
}
