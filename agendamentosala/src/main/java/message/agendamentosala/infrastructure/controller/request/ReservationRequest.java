package message.agendamentosala.infrastructure.controller.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import message.agendamentosala.domain.model.RoomName;

import java.time.LocalDateTime;

public record ReservationRequest(@NotNull(message = "Nome da sala é obrigatório")
                                 RoomName roomName,

                                 @NotNull(message = "O número de pessoas necessárias é obrigatório.")
                                 @Min(value = 1, message = "O número mínimo de pessoas exigido é 1.")
                                 int requiredPeople,

                                 @NotNull(message = "A data e hora de início são obrigatórias.")
                                 @Future(message = "A data e hora de início devem ser no futuro.")
                                 LocalDateTime startDateTime,

                                 @NotNull(message = "A data e hora de término são obrigatórias.")
                                 LocalDateTime endDateTime) {
}