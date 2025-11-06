package message.agendamentosala.application.usecase;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.infrastructure.gateway.messaging.ReservationProducerGateway;
import message.agendamentosala.infrastructure.gateway.persistence.UserPersistenceGateway;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScheduleReservationUseCase {

    private final UserPersistenceGateway persistenceGateway;
    private final ReservationProducerGateway producerGateway;

    public Reservation execute(String fullName, RoomName roomName, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        Reservation newReservation = new Reservation(
                null,
                fullName,
                roomName,
                startDateTime,
                endDateTime,
                ReservationStatus.PENDING
        );
        Reservation savedReservation = persistenceGateway.save(newReservation);
        producerGateway.sendCancellationDelay(savedReservation);

        return savedReservation;
    }
}
