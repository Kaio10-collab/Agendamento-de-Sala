package com.send.agendamentosala.application.room;

import message.agendamentosala.application.usecase.room.UpdateRoomUseCase;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateRoomUseCaseTest {

    @Mock
    private RoomPersistenceGateway persistenceGateway;

    @InjectMocks
    private UpdateRoomUseCase updateRoomUseCase;

    private final Long ROOM_ID = 1L;
    private final Long ANOTHER_ROOM_ID = 2L;
    private final RoomName OLD_NAME = RoomName.THOR;
    private final RoomName NEW_NAME = RoomName.HULK;

    private Room createMockRoom(Long id, RoomName name) {
        return new Room(id, name, RoomStatus.AVAILABLE);
    }

    private Room createMockUpdatedRoom(Long id, RoomName name) {
        return new Room(id, name, RoomStatus.AVAILABLE);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should update room name and save when the new name is different and not in use")
    void shouldUpdateRoomNameAndSaveWhenNewNameIsDifferentAndNotInUse() {

        var existingRoom = createMockRoom(ROOM_ID, OLD_NAME);
        var updatedRoom = createMockUpdatedRoom(ROOM_ID, NEW_NAME);

        when(persistenceGateway.findById(eq(ROOM_ID))).thenReturn(Optional.of(existingRoom));
        when(persistenceGateway.findByName(eq(NEW_NAME))).thenReturn(Optional.empty());
        when(persistenceGateway.save(any(Room.class))).thenReturn(updatedRoom);

        var result = updateRoomUseCase.execute(ROOM_ID, NEW_NAME);

        assertNotNull(result);
        assertEquals(ROOM_ID, result.id());
        assertEquals(NEW_NAME, result.name());

        verify(persistenceGateway, times(1)).findById(eq(ROOM_ID));
        verify(persistenceGateway, times(1)).findByName(eq(NEW_NAME));
        verify(persistenceGateway, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Should save when the new name is the same as the existing name (no check needed)")
    void shouldSaveWhenNewNameIsTheSameAsTheExistingName() {

        var existingRoom = createMockRoom(ROOM_ID, OLD_NAME);

        when(persistenceGateway.findById(eq(ROOM_ID))).thenReturn(Optional.of(existingRoom));
        when(persistenceGateway.save(any(Room.class))).thenReturn(existingRoom);

        var result = updateRoomUseCase.execute(ROOM_ID, OLD_NAME);

        assertNotNull(result);
        assertEquals(OLD_NAME, result.name());

        verify(persistenceGateway, times(1)).findById(eq(ROOM_ID));
        verify(persistenceGateway, never()).findByName(any());
        verify(persistenceGateway, times(1)).save(any(Room.class));
    }

    // ---------------------- CENÁRIOS DE EXCEÇÃO ----------------------

    @Test
    @DisplayName("Should throw ValidationException when room ID is not found")
    void shouldThrowValidationExceptionWhenRoomIdIsNotFound() {

        when(persistenceGateway.findById(eq(ROOM_ID))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            updateRoomUseCase.execute(ROOM_ID, NEW_NAME);
        });

        assertTrue(exception.getMessage().contains("Sala não encontrada para o ID: " + ROOM_ID));

        verify(persistenceGateway, times(1)).findById(eq(ROOM_ID));
        verify(persistenceGateway, never()).findByName(any());
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when new name is already in use by another room")
    void shouldThrowValidationExceptionWhenNewNameIsAlreadyInUseByAnotherRoom() {

        var existingRoom = createMockRoom(ROOM_ID, OLD_NAME);
        var conflictingRoom = createMockRoom(ANOTHER_ROOM_ID, NEW_NAME);

        when(persistenceGateway.findById(eq(ROOM_ID))).thenReturn(Optional.of(existingRoom));
        when(persistenceGateway.findByName(eq(NEW_NAME))).thenReturn(Optional.of(conflictingRoom));

        var exception = assertThrows(ValidationException.class, () -> {
            updateRoomUseCase.execute(ROOM_ID, NEW_NAME);
        });

        assertTrue(exception.getMessage().contains("Nome da sala " + NEW_NAME + " já está em uso."));

        verify(persistenceGateway, times(1)).findById(eq(ROOM_ID));
        verify(persistenceGateway, times(1)).findByName(eq(NEW_NAME));
        verify(persistenceGateway, never()).save(any());
    }
}