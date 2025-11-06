package message.agendamentosala.application.usecase.room;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateRoomUseCase {

    private final RoomPersistenceGateway persistenceGateway;

    public Room execute(Long id, RoomName newName) {

        Room existingRoom = persistenceGateway.findById(id)
                .orElseThrow(() -> new ValidationException("Não foi possível atualizar: Sala não encontrada para o ID: " + id));

        if (!existingRoom.name().equals(newName)) {
            if (persistenceGateway.findByName(newName).isPresent()) {
                throw new ValidationException("Não foi possível atualizar: Nome da sala " + newName + " já está em uso.");
            }
        }

        Room updatedRoom = new Room(id, newName, existingRoom.status());
        return persistenceGateway.save(updatedRoom);
    }
}