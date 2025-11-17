package com.send.agendamentosala.infrastructure;

import message.agendamentosala.application.usecase.checkin.ConfirmReservationUseCase;
import message.agendamentosala.application.usecase.checkin.TriggerCheckInUseCase;
import message.agendamentosala.application.usecase.reservation.CreateReservationUseCase;
import message.agendamentosala.application.usecase.reservation.DeleteReservationUseCase;
import message.agendamentosala.application.usecase.reservation.ReadReservationUseCase;
import message.agendamentosala.application.usecase.room.ListAvailableRoomsUseCase;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.controller.ReservationController;
import message.agendamentosala.infrastructure.controller.request.ReservationRequest;
import message.agendamentosala.infrastructure.controller.response.ReservationResponse;
import message.agendamentosala.infrastructure.controller.response.RoomResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private CreateReservationUseCase createReservationUseCase;

    @Mock
    private ReadReservationUseCase readReservationUseCase;

    @Mock
    private ConfirmReservationUseCase confirmReservationUseCase;

    @Mock
    private TriggerCheckInUseCase triggerCheckInUseCase;

    @Mock
    private ListAvailableRoomsUseCase listAvailableRoomsUseCase;

    @Mock
    private DeleteReservationUseCase deleteReservationUseCase;

    @InjectMocks
    private ReservationController reservationController;

    private final String TEST_EMAIL = "bruno.rocha@empresa.com";
    private final LocalDateTime TOMORROW = LocalDateTime.now().plusDays(1).toLocalDate().atTime(10, 0, 0);
    private final LocalDateTime START_TIME = TOMORROW;
    private final LocalDateTime END_TIME = TOMORROW.plusHours(1);
    private final RoomName ROOM_NAME = RoomName.THOR;

    private Reservation createMockReservation(RoomStatus status) {
        return new Reservation(
                1L, TEST_EMAIL, ROOM_NAME, 5, START_TIME, END_TIME, status
        );
    }

    private ReservationRequest createMockRequest() {
        return new ReservationRequest(
                ROOM_NAME, 5, START_TIME, END_TIME
        );
    }

    // ---------------------- CREATE (POST /) ----------------------

    @Test
    @DisplayName("Should return created status and STAND_BY reservation when creation succeeds")
    void shouldReturnCreatedStatusAndStandByReservationWhenCreationSucceeds() {

        var mockReservation = createMockReservation(RoomStatus.STAND_BY);
        var mockRequest = createMockRequest();

        when(createReservationUseCase.execute(
                eq(TEST_EMAIL), eq(ROOM_NAME), eq(5), eq(START_TIME), eq(END_TIME)
        )).thenReturn(mockReservation);

        ResponseEntity<ReservationResponse> response = reservationController.create(TEST_EMAIL, mockRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_EMAIL, response.getBody().userEmail());
        assertEquals(RoomStatus.STAND_BY, response.getBody().status());
        verify(createReservationUseCase, times(1)).execute(any(), any(), anyInt(), any(), any());
    }

    // ---------------------- CONFIRM (POST /confirm) ----------------------

    @Test
    @DisplayName("Should return OK status and PENDING reservation when confirmation succeeds")
    void shouldReturnOkStatusAndPendingReservationWhenConfirmationSucceeds() {

        var mockReservation = createMockReservation(RoomStatus.PENDING);

        when(confirmReservationUseCase.execute(eq(TEST_EMAIL))).thenReturn(mockReservation);

        ResponseEntity<ReservationResponse> response = reservationController.confirm(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(RoomStatus.PENDING, response.getBody().status());
        verify(confirmReservationUseCase, times(1)).execute(eq(TEST_EMAIL));
    }

    // ---------------------- CHECK-IN (POST /check-in) ----------------------

    @Test
    @DisplayName("Should return OK status and reservation when checkIn trigger succeeds")
    void shouldReturnOkStatusAndReservationWhenCheckInTriggerSucceeds() {

        var mockReservation = createMockReservation(RoomStatus.PENDING);

        when(triggerCheckInUseCase.execute(eq(TEST_EMAIL))).thenReturn(mockReservation);

        ResponseEntity<ReservationResponse> response = reservationController.checkIn(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(RoomStatus.PENDING, response.getBody().status());
        verify(triggerCheckInUseCase, times(1)).execute(eq(TEST_EMAIL));
    }

    // ---------------------- DELETE (DELETE /) ----------------------

    @Test
    @DisplayName("Should return NO_CONTENT status when deletion succeeds")
    void shouldReturnNoContentStatusWhenDeletionSucceeds() {

        doNothing().when(deleteReservationUseCase).execute(eq(TEST_EMAIL));

        ResponseEntity<Void> response = reservationController.delete(TEST_EMAIL);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteReservationUseCase, times(1)).execute(eq(TEST_EMAIL));
    }

    // ---------------------- READ (GET /user/{email}) ----------------------

    @Test
    @DisplayName("Should return OK status and list of active reservations when retrieval succeeds")
    void shouldReturnOkStatusAndListOfActiveReservationsWhenRetrievalSucceeds() {

        var mockReservation = createMockReservation(RoomStatus.PENDING);
        List<Reservation> mockList = List.of(mockReservation);

        when(readReservationUseCase.findActiveByEmail(eq(TEST_EMAIL))).thenReturn(mockList);

        ResponseEntity<List<ReservationResponse>> response = reservationController.findByUserEmail(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(TEST_EMAIL, response.getBody().getFirst().userEmail());
        verify(readReservationUseCase, times(1)).findActiveByEmail(eq(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should return OK status and empty list when no active reservations are found")
    void shouldReturnOkStatusAndEmptyListWhenNoActiveReservationsAreFound() {

        when(readReservationUseCase.findActiveByEmail(eq(TEST_EMAIL))).thenReturn(List.of());

        ResponseEntity<List<ReservationResponse>> response = reservationController.findByUserEmail(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(readReservationUseCase, times(1)).findActiveByEmail(eq(TEST_EMAIL));
    }

    // ---------------------- LIST AVAILABLE ROOMS (GET /available) ----------------------

    @Test
    @DisplayName("Should return OK status and list of available rooms when retrieval succeeds")
    void shouldReturnOkStatusAndListOfAvailableRoomsWhenRetrievalSucceeds() {

        var mockRoom = new Room(null, RoomName.HULK, RoomStatus.AVAILABLE);
        List<Room> mockList = List.of(mockRoom);

        when(listAvailableRoomsUseCase.execute(eq(START_TIME), eq(END_TIME))).thenReturn(mockList);

        ResponseEntity<List<RoomResponse>> response = reservationController.listAvailableRooms(START_TIME, END_TIME);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(RoomName.HULK, response.getBody().getFirst().name());
        verify(listAvailableRoomsUseCase, times(1)).execute(eq(START_TIME), eq(END_TIME));
    }

    @Test
    @DisplayName("Should return OK status and empty list when no rooms are available")
    void shouldReturnOkStatusAndEmptyListWhenNoRoomsAreAvailable() {

        when(listAvailableRoomsUseCase.execute(eq(START_TIME), eq(END_TIME))).thenReturn(List.of());

        ResponseEntity<List<RoomResponse>> response = reservationController.listAvailableRooms(START_TIME, END_TIME);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(listAvailableRoomsUseCase, times(1)).execute(eq(START_TIME), eq(END_TIME));
    }
}