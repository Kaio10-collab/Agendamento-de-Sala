package com.send.agendamentosala.application.reservation;

import message.agendamentosala.application.usecase.reservation.ProcessStandByCancellationUseCase;
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
class ProcessStandByCancellationUseCaseTest {

    @Mock
    private ReservationPersistenceGateway persistenceGateway;

    @InjectMocks
    private ProcessStandByCancellationUseCase processStandByCancellationUseCase;

    private final Long RESERVATION_ID = 50L;
    private final String TEST_EMAIL = "user@example.com";
    private final LocalDateTime START_TIME = LocalDateTime.now().plusHours(1);

    private Reservation createMockReservation(RoomStatus status) {
        return new Reservation(RESERVATION_ID, TEST_EMAIL, RoomName.HULK, 5, START_TIME, START_TIME.plusHours(1), status);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should change status to AVAILABLE and save when status is STAND_BY")
    void shouldChangeStatusToAvailableAndSaveWhenStatusIsStandBy() {

        var standByReservation = createMockReservation(RoomStatus.STAND_BY);

        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(standByReservation));
        when(persistenceGateway.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);

            if (saved.status() != RoomStatus.AVAILABLE) {
                throw new IllegalStateException("Status was not changed to AVAILABLE");
            }
            return saved;
        });

        processStandByCancellationUseCase.execute(RESERVATION_ID);

        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, times(1)).save(any(Reservation.class));
    }

    // ---------------------- CENÁRIOS DE IGNORAR/TOLERÂNCIA A FALHAS ----------------------

    @Test
    @DisplayName("Should ignore and not save when reservation ID is not found (null)")
    void shouldIgnoreAndNotSaveWhenReservationIdIsNotFound() {

        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.empty());

        processStandByCancellationUseCase.execute(RESERVATION_ID);

        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should ignore and not save when status is already PENDING (already confirmed)")
    void shouldIgnoreAndNotSaveWhenStatusIsAlreadyPending() {

        var pendingReservation = createMockReservation(RoomStatus.PENDING);
        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(pendingReservation));

        processStandByCancellationUseCase.execute(RESERVATION_ID);

        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should ignore and not save when status is CHECKED_IN")
    void shouldIgnoreAndNotSaveWhenStatusIsCheckedIn() {

        var checkedInReservation = createMockReservation(RoomStatus.CHECKED_IN);
        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(checkedInReservation));

        processStandByCancellationUseCase.execute(RESERVATION_ID);

        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }
}