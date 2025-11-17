package com.send.agendamentosala.application.checkin;

import message.agendamentosala.application.usecase.checkin.ProcessCheckInUseCase;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessCheckInUseCaseTest {

    @Mock
    private ReservationPersistenceGateway persistenceGateway;

    @InjectMocks
    private ProcessCheckInUseCase processCheckInUseCase;

    private final Long RESERVATION_ID = 15L;
    private final String TEST_EMAIL = "user@example.com";
    private final LocalDateTime START_TIME = LocalDateTime.now().plusDays(1).toLocalDate().atTime(10, 0, 0);
    private final LocalDateTime END_TIME = START_TIME.plusHours(1);

    private Reservation createMockReservation(RoomStatus status) {
        return new Reservation(RESERVATION_ID, TEST_EMAIL, RoomName.HULK, 5, START_TIME, END_TIME, status);
    }

    @Test
    @DisplayName("Should change status to CHECKED_IN and save when status is PENDING")
    void shouldChangeStatusToCheckedInAndSaveWhenStatusIsPending() {

        var pendingReservation = createMockReservation(RoomStatus.PENDING);

        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(pendingReservation));
        when(persistenceGateway.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            if (saved.status() != RoomStatus.CHECKED_IN) {
                throw new IllegalStateException("Status was not changed to CHECKED_IN");
            }
            return saved;
        });

        processCheckInUseCase.execute(RESERVATION_ID);

        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should ignore and not save when reservation ID is not found (null)")
    void shouldIgnoreAndNotSaveWhenReservationIdIsNotFound() {

        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.empty());

        processCheckInUseCase.execute(RESERVATION_ID);

        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should ignore and not save when status is already CHECKED_IN")
    void shouldIgnoreAndNotSaveWhenStatusIsAlreadyCheckedIn() {

        var checkedInReservation = createMockReservation(RoomStatus.CHECKED_IN);
        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(checkedInReservation));

        processCheckInUseCase.execute(RESERVATION_ID);

        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should ignore and not save when status is STAND_BY")
    void shouldIgnoreAndNotSaveWhenStatusIsStandBy() {

        var standByReservation = createMockReservation(RoomStatus.STAND_BY);
        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(standByReservation));

        processCheckInUseCase.execute(RESERVATION_ID);

        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }
}