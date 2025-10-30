package message.agendamentosala.application.usecase;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.entity.ReservationStatus;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.infrastructure.gateway.persistence.ReservationPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessCheckInUseCase {

    private final ReservationPersistenceGateway persistenceGateway;

    public String execute(Long reservationId) {

        Reservation reservation = persistenceGateway.findById(reservationId)
                .orElseThrow(() -> new ValidationException("Reservation not found for ID: " + reservationId));

        if (reservation.status() == ReservationStatus.CANCELED) {
            return "Reservation for room " + reservation.roomName() + " was already canceled.";
        }

        if (reservation.status() == ReservationStatus.CHECKED_IN) {
            return "Reservation for room " + reservation.roomName() + " was already checked-in.";
        }

        Reservation checkedInReservation = reservation.checkIn();
        persistenceGateway.save(checkedInReservation);

        return "A " + checkedInReservation.roomName() + " foi liberada para o cliente " + checkedInReservation.fullName();
    }
}
