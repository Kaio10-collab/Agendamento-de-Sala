package message.agendamentosala.application.usecase.checkin;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.messaging.ReservationProducerGateway;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TriggerCheckInUseCase {

    private final ReservationPersistenceGateway reservationGateway;
    private final ReservationProducerGateway producerGateway;

    public Reservation execute(String email) {

        LocalDateTime now = LocalDateTime.now();

        Reservation reservation = reservationGateway.findActiveReservationsByUserEmail(email).stream()
                .filter(r -> r.status() == RoomStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ValidationException("Nenhuma reserva PENDENTE encontrada para o e-mail: " + email));

        LocalDateTime validCheckInStart = reservation.startDateTime().minusMinutes(15);
        LocalDateTime validCheckInEnd = reservation.startDateTime().plusMinutes(15);

        if (now.isBefore(validCheckInStart)) {
            throw new ValidationException(
                    "O check-in ainda não está disponível. A janela de check-in abre às: " + validCheckInStart
            );
        }

        if (now.isAfter(validCheckInEnd)) {
            throw new ValidationException(
                    "O prazo para check-in expirou. O prazo era: " + validCheckInEnd
            );
        }

        producerGateway.sendCheckInConfirmation(reservation.id());
        return reservation;
    }
}