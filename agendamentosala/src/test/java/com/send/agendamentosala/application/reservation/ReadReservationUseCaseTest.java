package com.send.agendamentosala.application.reservation;

import message.agendamentosala.application.usecase.reservation.ReadReservationUseCase;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadReservationUseCaseTest {

    @Mock
    private ReservationPersistenceGateway persistenceGateway;

    @InjectMocks
    private ReadReservationUseCase readReservationUseCase;

    private final String VALID_EMAIL = "valid.user@empresa.com";
    private final String INVALID_EMAIL = "invalid-email";
    private final Long RESERVATION_ID = 5L;
    private final LocalDateTime START_TIME = LocalDateTime.now().plusHours(1);

    private Reservation createMockActiveReservation() {
        return new Reservation(RESERVATION_ID, VALID_EMAIL, RoomName.HULK, 5, START_TIME, START_TIME.plusHours(1), RoomStatus.PENDING);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should validate email and return a list of active reservations when found")
    void shouldValidateEmailAndReturnAListOfActiveReservationsWhenFound() {

        var activeReservation = createMockActiveReservation();
        List<Reservation> mockList = List.of(activeReservation);

        when(persistenceGateway.findActiveReservationsByUserEmail(eq(VALID_EMAIL))).thenReturn(mockList);

        List<Reservation> result = readReservationUseCase.findActiveByEmail(VALID_EMAIL);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(VALID_EMAIL, result.getFirst().userEmail());

        verify(persistenceGateway, times(1)).findActiveReservationsByUserEmail(eq(VALID_EMAIL));
    }

    @Test
    @DisplayName("Should validate email and return an empty list when no active reservations are found")
    void shouldValidateEmailAndReturnAnEmptyListWhenNoActiveReservationsAreFound() {

        when(persistenceGateway.findActiveReservationsByUserEmail(eq(VALID_EMAIL))).thenReturn(List.of());

        List<Reservation> result = readReservationUseCase.findActiveByEmail(VALID_EMAIL);

        assertTrue(result.isEmpty());

        verify(persistenceGateway, times(1)).findActiveReservationsByUserEmail(eq(VALID_EMAIL));
    }

    // ---------------------- CENÁRIO DE FALHA (Validação de E-mail) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when email format is invalid")
    void shouldThrowValidationExceptionWhenEmailFormatIsInvalid() {

        assertThrows(ValidationException.class, () -> {
            readReservationUseCase.findActiveByEmail(INVALID_EMAIL);
        });

        verify(persistenceGateway, never()).findActiveReservationsByUserEmail(any());
    }
}