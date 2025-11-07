package message.agendamentosala.application.usecase.reservation;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.messaging.ReservationProducerGateway;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
import message.agendamentosala.infrastructure.gateway.persistence.user.UserPersistenceGateway;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private final UserPersistenceGateway userGateway;
    private final RoomPersistenceGateway roomGateway;
    private final ReservationPersistenceGateway reservationGateway;
    private final ReservationProducerGateway producerGateway;

    public Reservation execute(String userEmail, RoomName roomName, int requiredPeople,
                               LocalDateTime startDateTime, LocalDateTime endDateTime) {

        userGateway.findByEmail(userEmail)
                .orElseThrow(() -> new ValidationException("Usuário não encontrado: " + userEmail));

        roomGateway.findByName(roomName)
                .orElseThrow(() -> new ValidationException("Sala não encontrada: " + roomName));

        if (requiredPeople > roomName.getCapacity()) {
            throw new ValidationException(
                    "A sala " + roomName + " possui a capacidade de limite de pessoas: " + roomName.getCapacity() +
                            ". Não é possível reservar para " + requiredPeople + " pessoas."
            );
        }

        if (!reservationGateway.findActiveReservationsByUserEmail(userEmail).isEmpty()) {
            throw new ValidationException("Usuário " + userEmail + " já possui uma reserva ativa.");
        }

        if (reservationGateway.hasConflict(roomName, startDateTime, endDateTime)) {
            throw new ValidationException("Para a seguinte sala: " + roomName + " Este horário já está reservado.");
        }

        Reservation newReservation = new Reservation(
                null,
                userEmail,
                roomName,
                requiredPeople,
                startDateTime,
                endDateTime,
                RoomStatus.STAND_BY
        );

        Reservation savedReservation = reservationGateway.save(newReservation);
        producerGateway.sendStandByTimeout(savedReservation);
        return savedReservation;
    }
}