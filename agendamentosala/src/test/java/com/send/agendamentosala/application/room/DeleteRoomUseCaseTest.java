package com.send.agendamentosala.application.room;

import message.agendamentosala.application.usecase.room.DeleteRoomUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteRoomUseCaseTest {

    @Mock
    private RoomPersistenceGateway persistenceGateway;

    @InjectMocks
    private DeleteRoomUseCase deleteRoomUseCase;

    private final Long ROOM_ID = 1L;

    private Room createMockRoom() {
        return new Room(ROOM_ID, RoomName.THOR, RoomStatus.AVAILABLE);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should find the room by ID and proceed with deletion when successful")
    void shouldFindTheRoomByIdAndProceedWithDeletionWhenSuccessful() {

        var existingRoom = createMockRoom();

        when(persistenceGateway.findById(eq(ROOM_ID))).thenReturn(Optional.of(existingRoom));

        doNothing().when(persistenceGateway).deleteById(eq(ROOM_ID));
        assertDoesNotThrow(() -> deleteRoomUseCase.execute(ROOM_ID));

        verify(persistenceGateway, times(1)).findById(eq(ROOM_ID));
        verify(persistenceGateway, times(1)).deleteById(eq(ROOM_ID));
    }

    // ---------------------- CENÁRIO DE EXCEÇÃO (Sala não encontrada) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when room ID is not found")
    void shouldThrowValidationExceptionWhenRoomIdIsNotFound() {

        when(persistenceGateway.findById(eq(ROOM_ID))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            deleteRoomUseCase.execute(ROOM_ID);
        });

        assertTrue(exception.getMessage().contains("Sala não encontrada para o ID: " + ROOM_ID));

        verify(persistenceGateway, times(1)).findById(eq(ROOM_ID));
        verify(persistenceGateway, never()).deleteById(any());
    }
}