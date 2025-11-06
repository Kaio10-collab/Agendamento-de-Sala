package message.agendamentosala.application.usecase.room;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadRoomUseCase {

    private final RoomPersistenceGateway persistenceGateway;

    public List<Room> findAll() {
        return persistenceGateway.findAll();
    }

    public Room findById(Long id) {
        return persistenceGateway.findById(id)
                .orElseThrow(() -> new ValidationException("Sala não encontrada para esse ID: " + id));
    }
}