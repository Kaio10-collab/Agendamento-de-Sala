package message.agendamentosala.domain.model;

import message.agendamentosala.domain.entity.ReservationStatus;
import message.agendamentosala.domain.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record Reservation(Long id,
                          String fullName,
                          RoomName roomName,
                          LocalDateTime startDateTime,
                          LocalDateTime endDateTime,
                          ReservationStatus status) {

    public Reservation {

        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Full name cannot be empty.");
        }

        // Validação da duração mínima de 30 minutos
        long durationMinutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime);
        if (durationMinutes < 30) {
            throw new ValidationException("A reserva deve ter pelo menos 30 minutos de duração.");
        }

        // Validação de horário de funcionamento das 08:00 às 18:00
        if (startDateTime.getHour() < 8 || endDateTime.getHour() > 18 ||
                (endDateTime.getHour() == 18 && endDateTime.getMinute() > 0) ) {

            throw new ValidationException("Os horários de reserva devem ser entre 08:00 AM e 18:00 PM.");
        }

        // Validação de reserva no mesmo dia
        if (!startDateTime.toLocalDate().isEqual(endDateTime.toLocalDate())) {
            throw new ValidationException("A reserva deve começar e terminar no mesmo dia.");
        }
    }

    // Método para mudar o status para CHECKED_IN
    public Reservation checkIn() {
        return new Reservation(id, fullName, roomName, startDateTime, endDateTime, ReservationStatus.CHECKED_IN);
    }

    // Método para mudar o status para CANCELED
    public Reservation cancel() {
        return new Reservation(id, fullName, roomName, startDateTime, endDateTime, ReservationStatus.CANCELED);
    }
}
