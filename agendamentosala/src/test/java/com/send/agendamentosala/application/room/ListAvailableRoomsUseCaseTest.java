package com.send.agendamentosala.application.room;

import message.agendamentosala.application.usecase.room.ListAvailableRoomsUseCase;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.persistence.reservation.ReservationPersistenceGateway;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
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
class ListAvailableRoomsUseCaseTest {

    @Mock
    private RoomPersistenceGateway roomGateway;

    @Mock
    private ReservationPersistenceGateway reservationGateway;

    @InjectMocks
    private ListAvailableRoomsUseCase listAvailableRoomsUseCase;

    private final LocalDateTime START_TIME = LocalDateTime.of(2026, 1, 10, 10, 0);
    private final LocalDateTime END_TIME = LocalDateTime.of(2026, 1, 10, 11, 0);

    private Room createMockRoom(Long id, RoomName name) {
        return new Room(id, name, RoomStatus.AVAILABLE);
    }

    // ---------------------- CENÁRIOS DE SUCESSO ----------------------

    @Test
    @DisplayName("Should return all rooms when no scheduling conflict exists for any room")
    void shouldReturnAllRoomsWhenNoSchedulingConflictExistsForAnyRoom() {

        var roomThor = createMockRoom(1L, RoomName.THOR);
        var roomHulk = createMockRoom(2L, RoomName.HULK);
        List<Room> allRooms = List.of(roomThor, roomHulk);

        when(roomGateway.findAll()).thenReturn(allRooms);
        when(reservationGateway.hasConflict(any(RoomName.class), eq(START_TIME), eq(END_TIME))).thenReturn(false);

        List<Room> availableRooms = listAvailableRoomsUseCase.execute(START_TIME, END_TIME);

        assertNotNull(availableRooms);
        assertEquals(2, availableRooms.size());
        assertTrue(availableRooms.contains(roomThor));
        assertTrue(availableRooms.contains(roomHulk));

        verify(roomGateway, times(1)).findAll();
        verify(reservationGateway, times(2)).hasConflict(any(), eq(START_TIME), eq(END_TIME));
    }

    @Test
    @DisplayName("Should filter out rooms that have a scheduling conflict and return only available ones")
    void shouldFilterOutRoomsThatHaveASchedulingConflictAndReturnOnlyAvailableOnes() {

        var roomThor = createMockRoom(1L, RoomName.THOR);
        var roomHulk = createMockRoom(2L, RoomName.HULK);
        List<Room> allRooms = List.of(roomThor, roomHulk);

        when(roomGateway.findAll()).thenReturn(allRooms);
        when(reservationGateway.hasConflict(eq(RoomName.THOR), eq(START_TIME), eq(END_TIME))).thenReturn(true);
        when(reservationGateway.hasConflict(eq(RoomName.HULK), eq(START_TIME), eq(END_TIME))).thenReturn(false);

        List<Room> availableRooms = listAvailableRoomsUseCase.execute(START_TIME, END_TIME);

        assertNotNull(availableRooms);
        assertEquals(1, availableRooms.size());
        assertEquals(RoomName.HULK, availableRooms.getFirst().name());
        assertFalse(availableRooms.contains(roomThor));

        verify(roomGateway, times(1)).findAll();
        verify(reservationGateway, times(1)).hasConflict(eq(RoomName.THOR), eq(START_TIME), eq(END_TIME));
        verify(reservationGateway, times(1)).hasConflict(eq(RoomName.HULK), eq(START_TIME), eq(END_TIME));
    }

    @Test
    @DisplayName("Should return an empty list when all rooms have a scheduling conflict")
    void shouldReturnAnEmptyListWhenAllRoomsHaveASchedulingConflict() {

        var roomThor = createMockRoom(1L, RoomName.THOR);
        List<Room> allRooms = List.of(roomThor);

        when(roomGateway.findAll()).thenReturn(allRooms);

        when(reservationGateway.hasConflict(any(RoomName.class), eq(START_TIME), eq(END_TIME))).thenReturn(true);

        List<Room> availableRooms = listAvailableRoomsUseCase.execute(START_TIME, END_TIME);

        assertNotNull(availableRooms);
        assertTrue(availableRooms.isEmpty());

        verify(roomGateway, times(1)).findAll();
        verify(reservationGateway, times(1)).hasConflict(eq(RoomName.THOR), eq(START_TIME), eq(END_TIME));
    }

    // ---------------------- CENÁRIOS DE EXCEÇÃO (Validação de Tempo) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when start time is after end time")
    void shouldThrowValidationExceptionWhenStartTimeIsAfterEndTime() {

        var invalidStartTime = END_TIME.plusMinutes(1);

        var exception = assertThrows(ValidationException.class, () -> {
            listAvailableRoomsUseCase.execute(invalidStartTime, END_TIME);
        });

        assertTrue(exception.getMessage().contains("O horário de início deve ser anterior ao horário de término."));

        verify(roomGateway, never()).findAll();
        verify(reservationGateway, never()).hasConflict(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw ValidationException when start time is equal to end time")
    void shouldThrowValidationExceptionWhenStartTimeIsEqualToEndTime() {

        var equalTime = START_TIME;

        var exception = assertThrows(ValidationException.class, () -> {
            listAvailableRoomsUseCase.execute(equalTime, equalTime);
        });

        assertTrue(exception.getMessage().contains("O horário de início deve ser anterior ao horário de término."));

        verify(roomGateway, never()).findAll();
        verify(reservationGateway, never()).hasConflict(any(), any(), any());
    }
}