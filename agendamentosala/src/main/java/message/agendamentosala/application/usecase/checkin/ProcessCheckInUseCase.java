package message.agendamentosala.application.usecase.checkin;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessCheckInUseCase {

    private final ReservationPersistenceGateway persistenceGateway;

    @Transactional
    public void execute(Long reservationId) {

        Reservation reservation = persistenceGateway.findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            System.out.println("CHECK-IN DO CONSUMER - ID da Reserva " + reservationId + " não encontrado. Ignorando.");
            return;
        }

        if (reservation.status() == RoomStatus.PENDING) {
            Reservation checkedInReservation = new Reservation(
                    reservation.id(),
                    reservation.userEmail(),
                    reservation.roomName(),
                    reservation.requiredPeople(),
                    reservation.startDateTime(),
                    reservation.endDateTime(),
                    RoomStatus.CHECKED_IN
            );

            persistenceGateway.save(checkedInReservation);
            System.out.println("CHECK-IN DO CONSUMER - ID da Reserva " + reservationId +
                    " status foi alterado de PENDING para CHECKED_IN.");

        } else {
            System.out.println("CHECK-IN DO CONSUMER - ID da Reserva " + reservationId +
                    " status já é " + reservation.status() + ". Ignorando.");
        }
    }
}