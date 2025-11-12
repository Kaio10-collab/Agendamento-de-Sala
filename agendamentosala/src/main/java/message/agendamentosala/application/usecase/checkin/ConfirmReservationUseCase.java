package message.agendamentosala.application.usecase.checkin;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmReservationUseCase {

    private final ReservationPersistenceGateway persistenceGateway;

    @Transactional // Serve para garantimos que a operação de busca e salvamento seja atômica
    public Reservation execute(Long reservationId) {

        Reservation reservation = persistenceGateway.findById(reservationId)
                .orElseThrow(() -> new ValidationException("Reserva não encontrada para o ID: " + reservationId));

        if (reservation.status() != RoomStatus.STAND_BY) {
            throw new ValidationException(
                    "Reserva ID " + reservationId + " Não é possível confirmar. O status atual é " + reservation.status() + "."
            );
        }

        Reservation confirmedReservation = reservation.confirmToPending();
        return persistenceGateway.save(confirmedReservation);
    }

    public Reservation execute(String userEmail) {
        Reservation standByReservation = persistenceGateway.findActiveReservationsByUserEmail(userEmail).stream()
                .filter(r -> r.status() == RoomStatus.STAND_BY)
                .findFirst()
                .orElseThrow(() -> new ValidationException("Nenhuma reserva STAND_BY encontrada para o e-mail: " + userEmail));

        return this.execute(standByReservation.id());
    }
}