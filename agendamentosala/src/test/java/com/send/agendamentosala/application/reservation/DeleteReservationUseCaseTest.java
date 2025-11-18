package com.send.agendamentosala.application.reservation;

import message.agendamentosala.application.usecase.reservation.DeleteReservationUseCase;
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
class DeleteReservationUseCaseTest {

    @Mock
    private ReservationPersistenceGateway persistenceGateway;

    @InjectMocks
    private DeleteReservationUseCase deleteReservationUseCase;

    private final String VALID_EMAIL = "valid.user@empresa.com";
    private final String INVALID_EMAIL = "invalid-email";
    private final Long RESERVATION_ID = 42L;
    private final LocalDateTime START_TIME = LocalDateTime.now().plusHours(1);

    private Reservation createMockActiveReservation() {
        return new Reservation(RESERVATION_ID, VALID_EMAIL, RoomName.HULK, 5, START_TIME, START_TIME.plusHours(1), RoomStatus.PENDING);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should validate email, find active reservation, and delete it when successful")
    void shouldValidateEmailFindActiveReservationAndDeleteItWhenSuccessful() {

        var activeReservation = createMockActiveReservation();

        when(persistenceGateway.findActiveReservationsByUserEmail(eq(VALID_EMAIL))).thenReturn(List.of(activeReservation));

        doNothing().when(persistenceGateway).deleteById(eq(RESERVATION_ID));
        assertDoesNotThrow(() -> deleteReservationUseCase.execute(VALID_EMAIL));

        verify(persistenceGateway, times(1)).findActiveReservationsByUserEmail(eq(VALID_EMAIL));
        verify(persistenceGateway, times(1)).deleteById(eq(RESERVATION_ID));
    }

    // ---------------------- CENÁRIOS DE FALHA (Regras de Negócio) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when no active reservation is found for the email")
    void shouldThrowValidationExceptionWhenNoActiveReservationIsFoundForTheEmail() {

        when(persistenceGateway.findActiveReservationsByUserEmail(eq(VALID_EMAIL))).thenReturn(List.of());

        var exception = assertThrows(ValidationException.class, () -> {
            deleteReservationUseCase.execute(VALID_EMAIL);
        });

        assertTrue(exception.getMessage().contains("Nenhuma reserva ativa encontrada para o e-mail:"));
        verify(persistenceGateway, times(1)).findActiveReservationsByUserEmail(eq(VALID_EMAIL));
        verify(persistenceGateway, never()).deleteById(any());
    }

    // ---------------------- CENÁRIO DE FALHA (Validação de E-mail) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when email format is invalid")
    void shouldThrowValidationExceptionWhenEmailFormatIsInvalid() {

        assertThrows(ValidationException.class, () -> {
            deleteReservationUseCase.execute(INVALID_EMAIL);
        });

        verify(persistenceGateway, never()).findActiveReservationsByUserEmail(any());
        verify(persistenceGateway, never()).deleteById(any());
    }
}