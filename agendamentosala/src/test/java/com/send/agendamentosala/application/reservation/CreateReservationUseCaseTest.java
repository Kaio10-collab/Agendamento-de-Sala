package com.send.agendamentosala.application.reservation;

import message.agendamentosala.application.usecase.reservation.CreateReservationUseCase;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.*;
import message.agendamentosala.infrastructure.gateway.messaging.ReservationProducerGateway;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
import message.agendamentosala.infrastructure.gateway.persistence.user.UserPersistenceGateway;
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
class CreateReservationUseCaseTest {

    @Mock
    private UserPersistenceGateway userGateway;

    @Mock
    private RoomPersistenceGateway roomGateway;

    @Mock
    private ReservationPersistenceGateway reservationGateway;

    @Mock
    private ReservationProducerGateway producerGateway;

    @InjectMocks
    private CreateReservationUseCase createReservationUseCase;

    private final String TEST_EMAIL = "user@example.com";
    private final RoomName ROOM_NAME = RoomName.THOR;
    private final int REQUIRED_PEOPLE = 5;
    private final int OVER_CAPACITY_PEOPLE = 11;
    private final Long RESERVATION_ID = 1L;

    private final LocalDateTime START_TIME = LocalDateTime.now().plusHours(1).withSecond(0).withNano(0);
    private final LocalDateTime END_TIME = START_TIME.plusHours(1);

    private User createMockUser() {
        return new User("Test User", TEST_EMAIL);
    }

    private Room createMockRoom() {
        return new Room(1L, ROOM_NAME, RoomStatus.AVAILABLE);
    }

    private Reservation createSavedReservation() {
        return new Reservation(RESERVATION_ID, TEST_EMAIL, ROOM_NAME, REQUIRED_PEOPLE, START_TIME, END_TIME, RoomStatus.STAND_BY);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should create reservation, set STAND_BY status, save, and send timeout message when all validations pass")
    void shouldCreateReservationSetStandByStatusSaveAndSendTimeoutMessageWhenAllValidationsPass() {

        when(userGateway.findByEmail(eq(TEST_EMAIL))).thenReturn(Optional.of(createMockUser()));
        when(roomGateway.findByName(eq(ROOM_NAME))).thenReturn(Optional.of(createMockRoom()));

        when(reservationGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL))).thenReturn(List.of());
        when(reservationGateway.hasConflict(eq(ROOM_NAME), eq(START_TIME), eq(END_TIME))).thenReturn(false);

        var savedReservation = createSavedReservation();
        when(reservationGateway.save(any(Reservation.class))).thenReturn(savedReservation);

        var result = createReservationUseCase.execute(TEST_EMAIL, ROOM_NAME, REQUIRED_PEOPLE, START_TIME, END_TIME);

        assertNotNull(result);
        assertEquals(RoomStatus.STAND_BY, result.status());
        verify(reservationGateway, times(1)).save(any(Reservation.class));
        verify(producerGateway, times(1)).sendStandByTimeout(eq(savedReservation));
    }

    // ---------------------- CENÁRIOS DE EXCEÇÃO ----------------------

    @Test
    @DisplayName("Should throw ValidationException when user is not found")
    void shouldThrowValidationExceptionWhenUserIsNotFound() {

        when(userGateway.findByEmail(eq(TEST_EMAIL))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            createReservationUseCase.execute(TEST_EMAIL, ROOM_NAME, REQUIRED_PEOPLE, START_TIME, END_TIME);
        });
        assertTrue(exception.getMessage().contains("Usuário não encontrado: " + TEST_EMAIL));
        verify(roomGateway, never()).findByName(any());
        verify(reservationGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when room is not found")
    void shouldThrowValidationExceptionWhenRoomIsNotFound() {

        when(userGateway.findByEmail(eq(TEST_EMAIL))).thenReturn(Optional.of(createMockUser()));
        when(roomGateway.findByName(eq(ROOM_NAME))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            createReservationUseCase.execute(TEST_EMAIL, ROOM_NAME, REQUIRED_PEOPLE, START_TIME, END_TIME);
        });
        assertTrue(exception.getMessage().contains("Sala não encontrada: " + ROOM_NAME));
        verify(reservationGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when required people exceeds room capacity")
    void shouldThrowValidationExceptionWhenRequiredPeopleExceedsRoomCapacity() {

        when(userGateway.findByEmail(eq(TEST_EMAIL))).thenReturn(Optional.of(createMockUser()));
        when(roomGateway.findByName(eq(ROOM_NAME))).thenReturn(Optional.of(createMockRoom()));

        var exception = assertThrows(ValidationException.class, () -> {
            createReservationUseCase.execute(TEST_EMAIL, ROOM_NAME, OVER_CAPACITY_PEOPLE, START_TIME, END_TIME);
        });
        assertTrue(exception.getMessage().contains("possui a capacidade de limite de pessoas"));
        verify(reservationGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when user already has an active reservation")
    void shouldThrowValidationExceptionWhenUserAlreadyHasAnActiveReservation() {

        when(userGateway.findByEmail(eq(TEST_EMAIL))).thenReturn(Optional.of(createMockUser()));
        when(roomGateway.findByName(eq(ROOM_NAME))).thenReturn(Optional.of(createMockRoom()));

        var activeReservation = createSavedReservation();
        when(reservationGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL))).thenReturn(List.of(activeReservation));

        var exception = assertThrows(ValidationException.class, () -> {
            createReservationUseCase.execute(TEST_EMAIL, ROOM_NAME, REQUIRED_PEOPLE, START_TIME, END_TIME);
        });
        assertTrue(exception.getMessage().contains("já possui uma reserva ativa."));
        verify(reservationGateway, times(1)).findActiveReservationsByUserEmail(eq(TEST_EMAIL));
        verify(reservationGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when a scheduling conflict exists for the room and time")
    void shouldThrowValidationExceptionWhenASchedulingConflictExistsForTheRoomAndTime() {

        when(userGateway.findByEmail(eq(TEST_EMAIL))).thenReturn(Optional.of(createMockUser()));
        when(roomGateway.findByName(eq(ROOM_NAME))).thenReturn(Optional.of(createMockRoom()));
        when(reservationGateway.findActiveReservationsByUserEmail(eq(TEST_EMAIL))).thenReturn(List.of());

        when(reservationGateway.hasConflict(eq(ROOM_NAME), eq(START_TIME), eq(END_TIME))).thenReturn(true);

        var exception = assertThrows(ValidationException.class, () -> {
            createReservationUseCase.execute(TEST_EMAIL, ROOM_NAME, REQUIRED_PEOPLE, START_TIME, END_TIME);
        });
        assertTrue(exception.getMessage().contains("Este horário já está reservado."));
        verify(reservationGateway, times(1)).hasConflict(eq(ROOM_NAME), eq(START_TIME), eq(END_TIME));
        verify(reservationGateway, never()).save(any());
    }
}