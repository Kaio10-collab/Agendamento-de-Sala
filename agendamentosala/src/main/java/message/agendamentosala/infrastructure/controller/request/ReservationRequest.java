package message.agendamentosala.infrastructure.controller.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import message.agendamentosala.domain.model.RoomName;

import java.time.LocalDateTime;

public record ReservationRequest (

        @NotBlank(message = "Nome completo é obrigatório")
        String fullName,

        @NotNull(message = "Nome da sala é obrigatório")
        RoomName roomName,

        @NotNull(message = "Data e hora de início são obrigatórias")
        @Future(message = "Deve ser no futuro")
        LocalDateTime startDateTime,

        @NotNull(message = "Data e hora de fim são obrigatórias")
        LocalDateTime endDateTime)
{ }
