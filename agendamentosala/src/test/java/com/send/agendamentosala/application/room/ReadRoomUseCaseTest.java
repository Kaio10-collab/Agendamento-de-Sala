package com.send.agendamentosala.application.room;

import message.agendamentosala.application.usecase.room.ReadRoomUseCase;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.gateway.persistence.room.RoomPersistenceGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadRoomUseCaseTest {

    @Mock
    private RoomPersistenceGateway persistenceGateway;

    @InjectMocks
    private ReadRoomUseCase readRoomUseCase;

    private final Long ROOM_ID = 5L;
    private final RoomName ROOM_NAME_THOR = RoomName.THOR;
    private final RoomName ROOM_NAME_HULK = RoomName.HULK;

    private Room createMockRoom(Long id, RoomName name) {
        return new Room(id, name, RoomStatus.AVAILABLE);
    }

    // ---------------------- MÉTODO: findAll() ----------------------

    @Test
    @DisplayName("Should return all rooms found by the persistence gateway")
    void shouldReturnAllRoomsFoundByThePersistenceGateway() {

        var room1 = createMockRoom(1L, ROOM_NAME_THOR);
        var room2 = createMockRoom(2L, ROOM_NAME_HULK);
        List<Room> expectedList = List.of(room1, room2);

        when(persistenceGateway.findAll()).thenReturn(expectedList);

        List<Room> result = readRoomUseCase.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(room1));

        verify(persistenceGateway, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return an empty list when no rooms are found")
    void shouldReturnAnEmptyListWhenNoRoomsAreFound() {

        when(persistenceGateway.findAll()).thenReturn(List.of());

        List<Room> result = readRoomUseCase.findAll();

        assertTrue(result.isEmpty());

        verify(persistenceGateway, times(1)).findAll();
    }

    // ---------------------- MÉTODO: findById(Long id) ----------------------

    @Test
    @DisplayName("Should return the room when a valid ID is provided and room is found")
    void shouldReturnTheRoomWhenAValidIdIsProvidedAndRoomIsFound() {

        var expectedRoom = createMockRoom(ROOM_ID, ROOM_NAME_THOR);

        when(persistenceGateway.findById(eq(ROOM_ID))).thenReturn(Optional.of(expectedRoom));

        var result = readRoomUseCase.findById(ROOM_ID);

        assertNotNull(result);
        assertEquals(ROOM_ID, result.id());
        assertEquals(ROOM_NAME_THOR, result.name());

        verify(persistenceGateway, times(1)).findById(eq(ROOM_ID));
    }

    @Test
    @DisplayName("Should throw ValidationException when room ID is not found")
    void shouldThrowValidationExceptionWhenRoomIdIsNotFound() {

        when(persistenceGateway.findById(eq(ROOM_ID))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            readRoomUseCase.findById(ROOM_ID);
        });
        assertTrue(exception.getMessage().contains("Sala não encontrada para esse ID: " + ROOM_ID));

        verify(persistenceGateway, times(1)).findById(eq(ROOM_ID));
    }
}