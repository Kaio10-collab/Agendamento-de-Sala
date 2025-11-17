package com.send.agendamentosala.application.checkin;

import message.agendamentosala.application.usecase.checkin.TriggerCheckInUseCase;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.messaging.ReservationProducerGateway;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerCheckInUseCaseTest {

    @Mock
    private ReservationPersistenceGateway reservationGateway;
    @Mock
    private ReservationProducerGateway producerGateway;

    @InjectMocks
    private TriggerCheckInUseCase triggerCheckInUseCase;

    private final Long RESERVATION_ID = 20L;
    private final String TEST_EMAIL = "user@example.com";
    private final RoomName ROOM_NAME = RoomName.THOR;

    private final LocalDateTime RESERVATION_START_TIME = LocalDateTime.now().plusMinutes(5).withNano(0).withSecond(0);

    private Reservation createMockReservation(RoomStatus status) {
        return new Reservation(RESERVATION_ID, TEST_EMAIL, ROOM_NAME, 5, RESERVATION_START_TIME, RESERVATION_START_TIME.plusHours(1), status);
    }

    // ---------------------- CENÁRIOS DE SUCESSO ----------------------

    @Test
    @DisplayName("Should trigger confirmation and return reservation when current time is within the check-in window")
    void shouldTriggerConfirmationAndReturnReservationWhenCurrentTimeIsWithinTheCheckInWindow() {

        var pendingReservation = createMockReservation(RoomStatus.PENDING);
        List<Reservation> activeReservations = List.of(pendingReservation);

        when(reservationGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL))).thenReturn(activeReservations);

        var result = triggerCheckInUseCase.execute(TEST_EMAIL);

        assertNotNull(result);
        assertEquals(RoomStatus.PENDING, result.status());

        verify(producerGateway, times(1)).sendCheckInConfirmation(eq(RESERVATION_ID));
        verify(reservationGateway, times(1)).findActiveReservationsByUserEmail(eq(TEST_EMAIL));
    }

    // ---------------------- CENÁRIOS DE FALHA DE VALIDAÇÃO DE NEGÓCIO ----------------------

    @Test
    @DisplayName("Should throw ValidationException when no PENDING reservation is found for email")
    void shouldThrowValidationExceptionWhenNoPendingReservationIsFoundForEmail() {

        var standByReservation = createMockReservation(RoomStatus.STAND_BY);

        when(reservationGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL)))
                .thenReturn(List.of(standByReservation));

        var exception = assertThrows(ValidationException.class, () -> {
            triggerCheckInUseCase.execute(TEST_EMAIL);
        });
        assertTrue(exception.getMessage().contains("Nenhuma reserva PENDENTE encontrada"));
        verify(producerGateway, never()).sendCheckInConfirmation(any());
    }

    // ---------------------- CENÁRIOS DE FALHA DE TEMPO ----------------------

    @Test
    @DisplayName("Should throw ValidationException when current time is before window start")
    void shouldThrowValidationExceptionWhenCurrentTimeIsBeforeWindowStart() {

        var distantStartTime = LocalDateTime.of(2050, 1, 1, 12, 0, 0);

        var futureReservation = new Reservation(
                RESERVATION_ID, TEST_EMAIL, ROOM_NAME, 5,
                distantStartTime, distantStartTime.plusHours(1),
                RoomStatus.PENDING
        );

        when(reservationGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL))).thenReturn(List.of(futureReservation));

        var exception = assertThrows(ValidationException.class, () -> {
            triggerCheckInUseCase.execute(TEST_EMAIL);
        });

        assertTrue(exception.getMessage().contains("O check-in ainda não está disponível. A janela de check-in abre às: "));
        assertTrue(exception.getMessage().contains("2050-01-01T11:45"));

        verify(producerGateway, never()).sendCheckInConfirmation(any());
        verify(reservationGateway, times(1)).findActiveReservationsByUserEmail(eq(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should throw ValidationException when current time is 1 second after window end")
    void shouldThrowValidationExceptionWhenCurrentTimeIsAfterWindowEnd() {

        var now = LocalDateTime.now();
        var pastStartTime = now.minusHours(2);
        var expiredReservation = new Reservation(RESERVATION_ID, TEST_EMAIL, ROOM_NAME, 5, pastStartTime, pastStartTime.plusHours(1), RoomStatus.PENDING);

        when(reservationGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL))).thenReturn(List.of(expiredReservation));

        var exception = assertThrows(ValidationException.class, () -> {
            triggerCheckInUseCase.execute(TEST_EMAIL);
        });
        assertTrue(exception.getMessage().contains("O prazo para check-in expirou. O prazo era: "));
        verify(producerGateway, never()).sendCheckInConfirmation(any());
    }
}