package message.agendamentosala.application.usecase.reservation;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.validator.EmailValidator;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadReservationUseCase {

    private final ReservationPersistenceGateway persistenceGateway;

    public List<Reservation> findActiveByEmail(String email) {
        EmailValidator.validate(email);

        List<Reservation> activeReservations = persistenceGateway.findActiveReservationsByUserEmail(email);

        if (activeReservations.isEmpty()) {
            return activeReservations;
        }

        return activeReservations;
    }
}