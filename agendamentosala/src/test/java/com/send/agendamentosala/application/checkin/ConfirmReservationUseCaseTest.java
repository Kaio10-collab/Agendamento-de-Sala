package com.send.agendamentosala.application.checkin;

import message.agendamentosala.application.usecase.checkin.ConfirmReservationUseCase;
import message.agendamentosala.domain.exception.ValidationException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmReservationUseCaseTest {

    @Mock
    private ReservationPersistenceGateway persistenceGateway;

    @InjectMocks
    private ConfirmReservationUseCase confirmReservationUseCase;

    private final Long RESERVATION_ID = 10L;
    private final String TEST_EMAIL = "user@example.com";
    private final LocalDateTime START_TIME = LocalDateTime.now().plusDays(1).toLocalDate().atTime(10, 0, 0);
    private final LocalDateTime END_TIME = START_TIME.plusHours(1);

    private Reservation createMockReservation(Long id, RoomStatus status) {
        return new Reservation(id, TEST_EMAIL, RoomName.HULK, 5, START_TIME, END_TIME, status);
    }

    // ---------------------- EXECUTE (Long reservationId) ----------------------

    @Test
    @DisplayName("Should change status to PENDING and save when reservation is STAND_BY")
    void shouldChangeStatusToPendingAndSaveWhenReservationIsStandBy() {

        var standByReservation = createMockReservation(RESERVATION_ID, RoomStatus.STAND_BY);

        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(standByReservation));
        when(persistenceGateway.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            assertEquals(RoomStatus.PENDING, saved.status()); // Verifica a transição
            return saved;
        });

        var result = confirmReservationUseCase.execute(RESERVATION_ID);

        assertNotNull(result);
        assertEquals(RoomStatus.PENDING, result.status());
        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when reservation ID is not found")
    void shouldThrowValidationExceptionWhenReservationIdIsNotFound() {

        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            confirmReservationUseCase.execute(RESERVATION_ID);
        });
        assertTrue(exception.getMessage().contains("Reserva não encontrada"));
        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when reservation status is already PENDING")
    void shouldThrowValidationExceptionWhenReservationStatusIsAlreadyPending() {

        var pendingReservation = createMockReservation(RESERVATION_ID, RoomStatus.PENDING);
        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(pendingReservation));

        var exception = assertThrows(ValidationException.class, () -> {
            confirmReservationUseCase.execute(RESERVATION_ID);
        });
        assertTrue(exception.getMessage().contains("Não é possível confirmar. O status atual é PENDING"));
        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when reservation status is CHECKED_IN")
    void shouldThrowValidationExceptionWhenReservationStatusIsCheckedIn() {

        var checkedInReservation = createMockReservation(RESERVATION_ID, RoomStatus.CHECKED_IN);
        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(checkedInReservation));

        var exception = assertThrows(ValidationException.class, () -> {
            confirmReservationUseCase.execute(RESERVATION_ID);
        });
        assertTrue(exception.getMessage().contains("Não é possível confirmar. O status atual é CHECKED_IN"));
        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, never()).save(any());
    }

    // ---------------------- EXECUTE (String userEmail) ----------------------

    @Test
    @DisplayName("Should delegate to execute(Long) when STAND_BY reservation is found by email")
    void shouldDelegateToExecuteLongWhenStandByReservationIsFoundByEmail() {

        var standByReservation = createMockReservation(RESERVATION_ID, RoomStatus.STAND_BY);
        var pendingReservation = createMockReservation(RESERVATION_ID, RoomStatus.PENDING);

        when(persistenceGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL)))
                .thenReturn(List.of(standByReservation));

        when(persistenceGateway.findById(eq(RESERVATION_ID))).thenReturn(Optional.of(standByReservation));
        when(persistenceGateway.save(any(Reservation.class))).thenReturn(pendingReservation);

        var result = confirmReservationUseCase.execute(TEST_EMAIL);

        assertNotNull(result);
        assertEquals(RoomStatus.PENDING, result.status());

        verify(persistenceGateway, times(1)).findActiveReservationsByUserEmail(eq(TEST_EMAIL));
        verify(persistenceGateway, times(1)).findById(eq(RESERVATION_ID));
        verify(persistenceGateway, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when no STAND_BY reservation is found for email")
    void shouldThrowValidationExceptionWhenNoStandByReservationIsFoundForEmail() {

        var pendingReservation = createMockReservation(RESERVATION_ID + 1, RoomStatus.PENDING);
        when(persistenceGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL)))
                .thenReturn(List.of(pendingReservation));

        var exception = assertThrows(ValidationException.class, () -> {
            confirmReservationUseCase.execute(TEST_EMAIL);
        });
        assertTrue(exception.getMessage().contains("Nenhuma reserva STAND_BY encontrada"));
        verify(persistenceGateway, times(1)).findActiveReservationsByUserEmail(eq(TEST_EMAIL));
        verify(persistenceGateway, never()).findById(any());
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when active reservation list is empty for email")
    void shouldThrowValidationExceptionWhenActiveReservationListIsEmptyForEmail() {

        when(persistenceGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL)))
                .thenReturn(List.of());

        var exception = assertThrows(ValidationException.class, () -> {
            confirmReservationUseCase.execute(TEST_EMAIL);
        });
        assertTrue(exception.getMessage().contains("Nenhuma reserva STAND_BY encontrada"));
        verify(persistenceGateway, times(1)).findActiveReservationsByUserEmail(eq(TEST_EMAIL));
        verify(persistenceGateway, never()).findById(any());
    }
}