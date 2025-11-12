package message.agendamentosala.domain.model;

import message.agendamentosala.domain.exception.ValidationException;

public record Room(
        Long id,
        RoomName name,
        RoomStatus status
) {

    public Room {
        if (id != null && status == null) {
            throw new ValidationException("O status da sala é obrigatório para salas existentes.");
        }
        if (name == null) {
            throw new ValidationException("O nome da sala é obrigatório.");
        }
    }

    public int getCapacity() {
        return name.getCapacity();
    }
}