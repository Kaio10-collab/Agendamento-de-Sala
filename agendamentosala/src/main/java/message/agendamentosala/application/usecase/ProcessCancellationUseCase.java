package message.agendamentosala.application.usecase;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.entity.ReservationStatus;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.infrastructure.gateway.persistence.ReservationPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessCancellationUseCase {

    private final ReservationPersistenceGateway persistenceGateway;

    public String execute(Long reservationId) {

        Reservation reservation = persistenceGateway.findById(reservationId)
                .orElseThrow(() -> new ValidationException("Reservation not found for ID: " + reservationId));

        if (reservation.status() == ReservationStatus.PENDING) {
            Reservation canceledReservation = reservation.cancel();
            persistenceGateway.save(canceledReservation);
            return "A " + canceledReservation.roomName() + " foi liberada para reserva, por falta de check-in";

        } else if (reservation.status() == ReservationStatus.CHECKED_IN) {
            return "A reserva de " + reservation.roomName() + " estava CONFIRMADA. Nenhuma ação de cancelamento foi necessária.";

        } else {
            return "A reserva de " + reservation.roomName() + " já estava CANCELADA.";
        }
    }
}
