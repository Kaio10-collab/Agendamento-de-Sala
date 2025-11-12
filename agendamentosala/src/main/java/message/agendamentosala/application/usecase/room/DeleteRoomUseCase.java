package message.agendamentosala.application.usecase.room;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteRoomUseCase {

    private final RoomPersistenceGateway persistenceGateway;

    public void execute(Long id) {

        if (persistenceGateway.findById(id).isEmpty()) {
            throw new ValidationException("Não foi possível excluir: Sala não encontrada para o ID: " + id);
        }
        persistenceGateway.deleteById(id);
    }
}