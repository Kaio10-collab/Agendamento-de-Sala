package message.agendamentosala.infrastructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import message.agendamentosala.application.usecase.checkin.ConfirmReservationUseCase;
import message.agendamentosala.application.usecase.reservation.CreateReservationUseCase;
import message.agendamentosala.application.usecase.reservation.DeleteReservationUseCase;
import message.agendamentosala.application.usecase.reservation.ListAvailableRoomsUseCase;
import message.agendamentosala.application.usecase.checkin.TriggerCheckInUseCase;
import message.agendamentosala.application.usecase.reservation.ReadReservationUseCase;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.infrastructure.controller.request.ReservationRequest;
import message.agendamentosala.infrastructure.controller.response.ReservationResponse;
import message.agendamentosala.infrastructure.controller.response.RoomResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final ReadReservationUseCase readReservationUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;
    private final TriggerCheckInUseCase triggerCheckInUseCase;
    private final ListAvailableRoomsUseCase listAvailableRoomsUseCase;
    private final DeleteReservationUseCase deleteReservationUseCase;

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.id(), reservation.userEmail(), reservation.roomName(),
                reservation.requiredPeople(), reservation.startDateTime(),
                reservation.endDateTime(), reservation.status()
        );
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> listAvailableRooms(
            @RequestParam LocalDateTime startDateTime,
            @RequestParam LocalDateTime endDateTime) {

        List<RoomResponse> response = listAvailableRoomsUseCase.execute(startDateTime, endDateTime).stream()
                .map(r -> new RoomResponse(null, r.name(), r.getCapacity(), r.status()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<ReservationResponse>> findByUserEmail(@PathVariable String email) {
        List<ReservationResponse> response = readReservationUseCase.findActiveByEmail(email).stream()
                .map(this::toResponse).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userEmail}")
    public ResponseEntity<ReservationResponse> create(@PathVariable String userEmail,
                                                      @Valid @RequestBody ReservationRequest request) {
        Reservation reservation = createReservationUseCase.execute(
                userEmail,
                request.roomName(),
                request.requiredPeople(),
                request.startDateTime(),
                request.endDateTime()
        );
        return new ResponseEntity<>(toResponse(reservation), HttpStatus.CREATED);
    }

    @PostMapping("/confirm/{userEmail}")
    public ResponseEntity<ReservationResponse> confirm(@PathVariable String userEmail) {
        Reservation reservation = confirmReservationUseCase.execute(userEmail);
        return ResponseEntity.ok(toResponse(reservation));
    }

    @PostMapping("/check-in/{userEmail}")
    public ResponseEntity<ReservationResponse> checkIn(@PathVariable String userEmail) {
        Reservation reservation = triggerCheckInUseCase.execute(userEmail);
        return ResponseEntity.ok(toResponse(reservation));
    }

    @DeleteMapping("/{userEmail}")
    public ResponseEntity<Void> delete(@PathVariable String userEmail) {
        deleteReservationUseCase.execute(userEmail);
        return ResponseEntity.noContent().build();
    }
}