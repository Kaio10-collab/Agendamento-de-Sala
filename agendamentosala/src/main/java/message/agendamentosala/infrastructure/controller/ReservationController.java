package message.agendamentosala.infrastructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import message.agendamentosala.application.usecase.ScheduleReservationUseCase;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.infrastructure.controller.request.ReservationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ScheduleReservationUseCase scheduleUseCase;

    @PostMapping
    public ResponseEntity<?> scheduleRoom(@Valid @RequestBody ReservationRequest request) {
        try {
            Reservation reservation = scheduleUseCase.execute(
                    request.fullName(),
                    request.roomName(),
                    request.startDateTime(),
                    request.endDateTime()
            );
            return new ResponseEntity<>(reservation, HttpStatus.CREATED);

        } catch (ValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
