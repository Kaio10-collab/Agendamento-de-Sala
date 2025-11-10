package message.agendamentosala.infrastructure.controller.response;

import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;

public record RoomResponse(Long id,
                           RoomName name,
                           int capacity,
                           RoomStatus status) {
}