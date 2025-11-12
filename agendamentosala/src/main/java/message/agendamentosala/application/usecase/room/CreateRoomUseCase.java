package message.agendamentosala.application.usecase.room;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateRoomUseCase {

    private final RoomPersistenceGateway persistenceGateway;

    public Room execute(RoomName name) {

        if (persistenceGateway.findByName(name).isPresent()) {
            throw new ValidationException("Sala com este nome " + name + " já existe.");
        }

        Room newRoom = new Room(null, name, RoomStatus.AVAILABLE);
        return persistenceGateway.save(newRoom);
    }
}