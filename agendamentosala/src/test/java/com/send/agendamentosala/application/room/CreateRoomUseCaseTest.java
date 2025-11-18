package com.send.agendamentosala.application.room;

import message.agendamentosala.application.usecase.room.CreateRoomUseCase;
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
class CreateRoomUseCaseTest {

    @Mock
    private RoomPersistenceGateway persistenceGateway;

    @InjectMocks
    private CreateRoomUseCase createRoomUseCase;

    private final RoomName ROOM_NAME = RoomName.THOR;
    private final Long ROOM_ID = 1L;

    private Room createMockNewRoom() {
        return new Room(null, ROOM_NAME, RoomStatus.AVAILABLE);
    }

    private Room createMockSavedRoom() {
        return new Room(ROOM_ID, ROOM_NAME, RoomStatus.AVAILABLE);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should save the new room with AVAILABLE status when name does not exist")
    void shouldSaveTheNewRoomWithAvailableStatusWhenNameDoesNotExist() {

        when(persistenceGateway.findByName(eq(ROOM_NAME))).thenReturn(Optional.empty());
        when(persistenceGateway.save(any(Room.class))).thenReturn(createMockSavedRoom());

        var result = createRoomUseCase.execute(ROOM_NAME);

        assertNotNull(result);
        assertEquals(ROOM_ID, result.id());
        assertEquals(ROOM_NAME, result.name());
        assertEquals(RoomStatus.AVAILABLE, result.status());

        verify(persistenceGateway, times(1)).findByName(eq(ROOM_NAME));
        verify(persistenceGateway, times(1)).save(any(Room.class));
    }

    // ---------------------- CENÁRIO DE EXCEÇÃO (Sala já existe) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when room with the same name already exists")
    void shouldThrowValidationExceptionWhenRoomWithTheSameNameAlreadyExists() {

        var existingRoom = createMockSavedRoom();

        when(persistenceGateway.findByName(eq(ROOM_NAME))).thenReturn(Optional.of(existingRoom));

        var exception = assertThrows(ValidationException.class, () -> {
            createRoomUseCase.execute(ROOM_NAME);
        });

        assertTrue(exception.getMessage().contains("Sala com este nome " + ROOM_NAME + " já existe."));

        verify(persistenceGateway, times(1)).findByName(eq(ROOM_NAME));
        verify(persistenceGateway, never()).save(any());
    }
}