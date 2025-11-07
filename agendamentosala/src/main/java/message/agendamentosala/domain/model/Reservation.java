package message.agendamentosala.domain.model;

import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.validator.EmailValidator;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record Reservation(Long id,
                          String userEmail,
                          RoomName roomName,
                          int requiredPeople,
                          LocalDateTime startDateTime,
                          LocalDateTime endDateTime,
                          RoomStatus status
) {
    public Reservation {

        EmailValidator.validate(userEmail);

        if (startDateTime == null || endDateTime == null) {
            throw new ValidationException("Os horários de início e término são obrigatórios.");
        }
        if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
            throw new ValidationException("O horário final não pode ser anterior ou igual ao horário inicial.");
        }

        if (startDateTime.toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new ValidationException("O dia selecionado não pode ser anterior ao dia atual.");
        }

        if (startDateTime.getHour() < 8 ||
                endDateTime.getHour() > 18 ||
                (endDateTime.getHour() == 18 && endDateTime.getMinute() > 0)) {

            throw new ValidationException("O tempo permitido para reserva é das 08:00 até às 18:00.");
        }

        long durationMinutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime);
        if (durationMinutes < 30) {
            throw new ValidationException("O tempo mínimo para reserva é de 30 min.");
        }

        if (requiredPeople <= 0) {
            throw new ValidationException("O número de pessoas necessárias deve ser maior que zero.");
        }
    }

    public Reservation confirmToPending() {
        if (this.status != RoomStatus.STAND_BY) {
            throw new ValidationException("Somente reservas em 'STAND_BY' podem ser confirmadas como 'PENDING'");
        }
        return new Reservation(id, userEmail, roomName, requiredPeople, startDateTime, endDateTime, RoomStatus.PENDING);
    }
}