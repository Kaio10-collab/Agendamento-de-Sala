package message.agendamentosala.application.usecase.reservation;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListAvailableRoomsUseCase {

    private final RoomPersistenceGateway roomGateway;
    private final ReservationPersistenceGateway reservationGateway;

    public List<Room> execute(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        if (startDateTime.isAfter(endDateTime) || startDateTime.isEqual(endDateTime)) {
            throw new ValidationException("O horário de início deve ser anterior ao horário de término.");
        }

        List<Room> allRooms = roomGateway.findAll();

        return allRooms.stream()
                .filter(room -> !reservationGateway.hasConflict(room.name(), startDateTime, endDateTime))
                .collect(Collectors.toList());
    }
}