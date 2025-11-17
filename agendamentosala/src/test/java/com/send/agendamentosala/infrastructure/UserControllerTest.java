package com.send.agendamentosala.infrastructure;

import message.agendamentosala.application.usecase.user.CreateUserUseCase;
import message.agendamentosala.application.usecase.user.DeleteUserUseCase;
import message.agendamentosala.application.usecase.user.ReadUserUseCase;
import message.agendamentosala.application.usecase.user.UpdateUserUseCase;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.User;
import message.agendamentosala.infrastructure.controller.UserController;
import message.agendamentosala.infrastructure.controller.request.UserRequest;
import message.agendamentosala.infrastructure.controller.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CreateUserUseCase createUserUseCase;
    @Mock
    private ReadUserUseCase readUserUseCase;
    @Mock
    private UpdateUserUseCase updateUserUseCase;
    @Mock
    private DeleteUserUseCase deleteUserUseCase;

    @InjectMocks
    private UserController userController;

    private final String TEST_EMAIL = "bruno.rocha@empresa.com";
    private final String TEST_NAME = "Bruno Rocha";
    private final String NEW_EMAIL = "bruno.silva@empresa.com";
    private final String NEW_NAME = "Bruno Silva";

    private User createMockUser(String email, String fullName) {
        return new User(fullName, email);
    }

    private UserRequest createMockRequest(String email, String fullName) {
        return new UserRequest(fullName, email);
    }

    // ---------------------- CREATE (POST /) ----------------------

    @Test
    @DisplayName("Should return created status and user response when creation succeeds")
    void shouldReturnCreatedStatusAndUserResponseWhenCreationSucceeds() {

        var mockUser = createMockUser(TEST_EMAIL, TEST_NAME);
        var mockRequest = createMockRequest(TEST_EMAIL, TEST_NAME);

        when(createUserUseCase.execute(eq(TEST_NAME), eq(TEST_EMAIL))).thenReturn(mockUser);

        ResponseEntity<UserResponse> response = userController.create(mockRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_EMAIL, response.getBody().email());
        assertEquals(TEST_NAME, response.getBody().fullName());
        verify(createUserUseCase, times(1)).execute(eq(TEST_NAME), eq(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should propagate validation exception when user creation fails")
    void shouldPropagateValidationExceptionWhenUserCreationFailed() {

        var mockRequest = createMockRequest(TEST_EMAIL, TEST_NAME);

        doThrow(new ValidationException("Email already registered")).when(createUserUseCase).execute(eq(TEST_NAME), eq(TEST_EMAIL));

        assertThrows(ValidationException.class, () -> {
            userController.create(mockRequest);
        });
        verify(createUserUseCase, times(1)).execute(eq(TEST_NAME), eq(TEST_EMAIL));
    }

    // ---------------------- READ (GET /{email}) ----------------------

    @Test
    @DisplayName("Should return OK status and user response when finding by email succeeds")
    void shouldReturnOkStatusAndUserResponseWhenFindingByEmailSucceeds() {

        var mockUser = createMockUser(TEST_EMAIL, TEST_NAME);

        when(readUserUseCase.execute(eq(TEST_EMAIL))).thenReturn(mockUser);

        ResponseEntity<UserResponse> response = userController.findByEmail(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_EMAIL, response.getBody().email());
        verify(readUserUseCase, times(1)).execute(eq(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should propagate validation exception when user is not found")
    void shouldPropagateValidationExceptionWhenUserIsNotFound() {

        doThrow(new ValidationException("User not found")).when(readUserUseCase).execute(eq(TEST_EMAIL));

        assertThrows(ValidationException.class, () -> {
            userController.findByEmail(TEST_EMAIL);
        });
        verify(readUserUseCase, times(1)).execute(eq(TEST_EMAIL));
    }

    // ---------------------- UPDATE (PUT /{currentEmail}) ----------------------

    @Test
    @DisplayName("Should return OK status and updated user response when update succeeds")
    void shouldReturnOkStatusAndUpdatedUserResponseWhenUpdateSucceeds() {

        var updatedUser = createMockUser(NEW_EMAIL, NEW_NAME);
        var updateRequest = createMockRequest(NEW_EMAIL, NEW_NAME);

        when(updateUserUseCase.execute(eq(TEST_EMAIL), eq(NEW_NAME), eq(NEW_EMAIL))).thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.update(TEST_EMAIL, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(NEW_EMAIL, response.getBody().email());
        assertEquals(NEW_NAME, response.getBody().fullName());
        verify(updateUserUseCase, times(1)).execute(eq(TEST_EMAIL), eq(NEW_NAME), eq(NEW_EMAIL));
    }

    @Test
    @DisplayName("Should propagate validation exception when update fails due to invalid email")
    void shouldPropagateValidationExceptionWhenUpdateFailsDueToInvalidEmail() {

        var updateRequest = createMockRequest(NEW_EMAIL, NEW_NAME);

        doThrow(new ValidationException("Cannot update: New email already in use")).when(updateUserUseCase)
                .execute(eq(TEST_EMAIL), eq(NEW_NAME), eq(NEW_EMAIL));

        assertThrows(ValidationException.class, () -> {
            userController.update(TEST_EMAIL, updateRequest);
        });
        verify(updateUserUseCase, times(1)).execute(eq(TEST_EMAIL), eq(NEW_NAME), eq(NEW_EMAIL));
    }

    // ---------------------- DELETE (DELETE /{email}) ----------------------

    @Test
    @DisplayName("Should return NO_CONTENT status when user deletion succeeds")
    void shouldReturnNoContentStatusWhenUserDeletionSucceeds() {

        doNothing().when(deleteUserUseCase).execute(eq(TEST_EMAIL));

        ResponseEntity<Void> response = userController.delete(TEST_EMAIL);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteUserUseCase, times(1)).execute(eq(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should propagate validation exception when deletion fails due to user not found")
    void shouldPropagateValidationExceptionWhenDeletionFailsDueToUserNotFound() {

        doThrow(new ValidationException("User not found")).when(deleteUserUseCase).execute(eq(TEST_EMAIL));

        assertThrows(ValidationException.class, () -> {
            userController.delete(TEST_EMAIL);
        });
        verify(deleteUserUseCase, times(1)).execute(eq(TEST_EMAIL));
    }
}