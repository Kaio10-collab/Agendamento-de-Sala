package message.agendamentosala.infrastructure.controller.response;

import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;

import java.time.LocalDateTime;

public record ReservationResponse(Long id,
                                  String userEmail,
                                  RoomName roomName,
                                  int requiredPeople,
                                  LocalDateTime startDateTime,
                                  LocalDateTime endDateTime,
                                  RoomStatus status) {
}