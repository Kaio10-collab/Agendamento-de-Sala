package com.send.agendamentosala.infrastructure;

import message.agendamentosala.application.usecase.room.CreateRoomUseCase;
import message.agendamentosala.application.usecase.room.DeleteRoomUseCase;
import message.agendamentosala.application.usecase.room.ReadRoomUseCase;
import message.agendamentosala.application.usecase.room.UpdateRoomUseCase;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.controller.RoomController;
import message.agendamentosala.infrastructure.controller.response.RoomResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private CreateRoomUseCase createRoomUseCase;

    @Mock
    private ReadRoomUseCase readRoomUseCase;

    @Mock
    private UpdateRoomUseCase updateRoomUseCase;

    @Mock
    private DeleteRoomUseCase deleteRoomUseCase;

    @InjectMocks
    private RoomController roomController;

    private final Long ROOM_ID = 1L;
    private final RoomName ROOM_NAME = RoomName.THOR;
    private final RoomName NEW_ROOM_NAME = RoomName.HULK;

    private Room createMockRoom(RoomName name, RoomStatus status) {
        return new Room(ROOM_ID, name, status);
    }

    // ---------------------- CREATE (POST /roomName) ----------------------

    @Test
    @DisplayName("Should return created status and room response when room creation succeeds")
    void shouldReturnCreatedStatusAndRoomResponseWhenRoomCreationSucceeds() {

        var mockRoom = createMockRoom(ROOM_NAME, RoomStatus.AVAILABLE);
        when(createRoomUseCase.execute(eq(ROOM_NAME))).thenReturn(mockRoom);

        ResponseEntity<RoomResponse> response = roomController.create(ROOM_NAME);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ROOM_NAME, response.getBody().name());
        assertEquals(RoomStatus.AVAILABLE, response.getBody().status());
        verify(createRoomUseCase, times(1)).execute(eq(ROOM_NAME));
    }

    @Test
    @DisplayName("Should propagate validation exception when room creation fails")
    void shouldPropagateValidationExceptionWhenRoomCreationFails() {

        doThrow(new ValidationException("Room already exists")).when(createRoomUseCase).execute(eq(ROOM_NAME));

        assertThrows(ValidationException.class, () -> {
            roomController.create(ROOM_NAME);
        });
        verify(createRoomUseCase, times(1)).execute(eq(ROOM_NAME));
    }

    // ---------------------- READ ALL (GET /) ----------------------

    @Test
    @DisplayName("Should return OK status and list of room responses when find all succeeds")
    void shouldReturnOkStatusAndListOfRoomResponsesWhenFindAllSucceeds() {

        var room1 = createMockRoom(ROOM_NAME, RoomStatus.AVAILABLE);
        var room2 = createMockRoom(NEW_ROOM_NAME, RoomStatus.AVAILABLE);
        List<Room> mockList = List.of(room1, room2);

        when(readRoomUseCase.findAll()).thenReturn(mockList);

        ResponseEntity<List<RoomResponse>> response = roomController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().anyMatch(r -> r.name() == ROOM_NAME));
        verify(readRoomUseCase, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return OK status and empty list when no rooms are found")
    void shouldReturnOkStatusAndEmptyListWhenNoRoomsAreFound() {

        when(readRoomUseCase.findAll()).thenReturn(List.of());

        ResponseEntity<List<RoomResponse>> response = roomController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(readRoomUseCase, times(1)).findAll();
    }

    // ---------------------- UPDATE (PUT /{id}/{newName}) ----------------------

    @Test
    @DisplayName("Should return OK status and updated room response when update succeeds")
    void shouldReturnOkStatusAndUpdatedRoomResponseWhenUpdateSucceeds() {

        var updatedRoom = createMockRoom(NEW_ROOM_NAME, RoomStatus.AVAILABLE);
        when(updateRoomUseCase.execute(eq(ROOM_ID), eq(NEW_ROOM_NAME))).thenReturn(updatedRoom);

        ResponseEntity<RoomResponse> response = roomController.update(ROOM_ID, NEW_ROOM_NAME);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(NEW_ROOM_NAME, response.getBody().name());
        verify(updateRoomUseCase, times(1)).execute(eq(ROOM_ID), eq(NEW_ROOM_NAME));
    }

    @Test
    @DisplayName("Should propagate validation exception when room update fails")
    void shouldPropagateValidationExceptionWhenRoomUpdateFails() {

        doThrow(new ValidationException("Room not found")).when(updateRoomUseCase).execute(eq(ROOM_ID), eq(NEW_ROOM_NAME));

        assertThrows(ValidationException.class, () -> {
            roomController.update(ROOM_ID, NEW_ROOM_NAME);
        });
        verify(updateRoomUseCase, times(1)).execute(eq(ROOM_ID), eq(NEW_ROOM_NAME));
    }

    // ---------------------- DELETE (DELETE /{id}) ----------------------

    @Test
    @DisplayName("Should return NO_CONTENT status when room deletion succeeds")
    void shouldReturnNoContentStatusWhenRoomDeletionSucceeds() {

        doNothing().when(deleteRoomUseCase).execute(eq(ROOM_ID));

        ResponseEntity<Void> response = roomController.delete(ROOM_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteRoomUseCase, times(1)).execute(eq(ROOM_ID));
    }

    @Test
    @DisplayName("Should propagate validation exception when room deletion fails")
    void shouldPropagateValidationExceptionWhenRoomDeletionFails() {

        doThrow(new ValidationException("Room cannot be deleted")).when(deleteRoomUseCase).execute(eq(ROOM_ID));

        assertThrows(ValidationException.class, () -> {
            roomController.delete(ROOM_ID);
        });
        verify(deleteRoomUseCase, times(1)).execute(eq(ROOM_ID));
    }
}