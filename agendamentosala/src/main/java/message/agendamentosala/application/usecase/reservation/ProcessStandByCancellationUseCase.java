package message.agendamentosala.application.usecase.reservation;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessStandByCancellationUseCase {

    private final ReservationPersistenceGateway persistenceGateway;

    @Transactional
    public void execute(Long reservationId) {

        Reservation reservation = persistenceGateway.findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            System.out.println("Cancelamento do CONSUMER - ID da reserva: " + reservationId + " Não encontrado. Ignorando.");
            return;
        }

        if (reservation.status() == RoomStatus.STAND_BY) {

            Reservation cancelledReservation = new Reservation(
                    reservation.id(),
                    reservation.userEmail(),
                    reservation.roomName(),
                    reservation.requiredPeople(),
                    reservation.startDateTime(),
                    reservation.endDateTime(),
                    RoomStatus.AVAILABLE
            );

            persistenceGateway.save(cancelledReservation);

            System.out.println("Cancelamento do CONSUMER - ID da reserva: " + reservationId +
                    " Tempo limite excedido. O status foi alterado de STAND_BY para AVAILABLE..");

        } else {
            System.out.println("Cancelamento do CONSUMER - ID da reserva: " + reservationId +
                    " O status já é: " + reservation.status() + ". Ignorando TTL message.");
        }
    }
}