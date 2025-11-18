package com.send.agendamentosala.application.user;

import message.agendamentosala.application.usecase.user.UpdateUserUseCase;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.User;
import message.agendamentosala.infrastructure.gateway.persistence.user.UserPersistenceGateway;
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
class UpdateUserUseCaseTest {

    @Mock
    private UserPersistenceGateway persistenceGateway;

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    private final String CURRENT_EMAIL = "bruno.rocha@empresa.com";
    private final String CURRENT_NAME = "Bruno Rocha";
    private final String NEW_EMAIL = "bruno.silva@empresa.com";
    private final String NEW_NAME = "Bruno Silva";
    private final String INVALID_EMAIL = "invalid-email";

    private User createMockUser(String email, String fullName) {
        return new User(fullName, email);
    }

    // ---------------------- CENÁRIO DE SUCESSO (APENAS NOME) ----------------------

    @Test
    @DisplayName("Should update only the full name and save when email remains the same")
    void shouldUpdateOnlyTheFullNameAndSaveWhenEmailRemainsTheSame() {

        var existingUser = createMockUser(CURRENT_EMAIL, CURRENT_NAME);
        var updatedUser = createMockUser(CURRENT_EMAIL, NEW_NAME);

        when(persistenceGateway.findByEmail(eq(CURRENT_EMAIL))).thenReturn(Optional.of(existingUser));
        when(persistenceGateway.save(any(User.class))).thenReturn(updatedUser);

        var result = updateUserUseCase.execute(CURRENT_EMAIL, NEW_NAME, CURRENT_EMAIL);

        assertNotNull(result);
        assertEquals(NEW_NAME, result.fullName());
        assertEquals(CURRENT_EMAIL, result.email());

        verify(persistenceGateway, times(1)).findByEmail(eq(CURRENT_EMAIL));
        verify(persistenceGateway, never()).deleteByEmail(any());
        verify(persistenceGateway, times(1)).save(any(User.class));
    }

    // ---------------------- CENÁRIO DE SUCESSO (NOME E E-MAIL) ----------------------

    @Test
    @DisplayName("Should update name and email, delete old record, and save new one when email changes and is available")
    void shouldUpdateNameAndEmailDeleteOldRecordAndSaveNewOneWhenEmailChangesAndIsAvailable() {

        var existingUser = createMockUser(CURRENT_EMAIL, CURRENT_NAME);
        var newUpdatedUser = createMockUser(NEW_EMAIL, NEW_NAME);

        when(persistenceGateway.findByEmail(eq(CURRENT_EMAIL))).thenReturn(Optional.of(existingUser));
        when(persistenceGateway.findByEmail(eq(NEW_EMAIL))).thenReturn(Optional.empty());

        doNothing().when(persistenceGateway).deleteByEmail(eq(CURRENT_EMAIL));

        when(persistenceGateway.save(any(User.class))).thenReturn(newUpdatedUser);

        var result = updateUserUseCase.execute(CURRENT_EMAIL, NEW_NAME, NEW_EMAIL);

        assertNotNull(result);
        assertEquals(NEW_NAME, result.fullName());
        assertEquals(NEW_EMAIL, result.email()); // E-mail deve ser o novo

        verify(persistenceGateway, times(1)).findByEmail(eq(CURRENT_EMAIL));
        verify(persistenceGateway, times(1)).findByEmail(eq(NEW_EMAIL));
        verify(persistenceGateway, times(1)).deleteByEmail(eq(CURRENT_EMAIL));
        verify(persistenceGateway, times(1)).save(any(User.class));
    }

    // ---------------------- CENÁRIOS DE EXCEÇÃO ----------------------

    @Test
    @DisplayName("Should throw ValidationException when user is not found by current email")
    void shouldThrowValidationExceptionWhenUserIsNotFoundByCurrentEmail() {

        when(persistenceGateway.findByEmail(eq(CURRENT_EMAIL))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            updateUserUseCase.execute(CURRENT_EMAIL, NEW_NAME, NEW_EMAIL);
        });

        assertTrue(exception.getMessage().contains("usuário não encontrado para o e-mail: " + CURRENT_EMAIL));

        verify(persistenceGateway, times(1)).findByEmail(eq(CURRENT_EMAIL));
        verify(persistenceGateway, never()).deleteByEmail(any());
        verify(persistenceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when new email is already in use by another user")
    void shouldThrowValidationExceptionWhenNewEmailIsAlreadyInUseByAnotherUser() {

        var existingUser = createMockUser(CURRENT_EMAIL, CURRENT_NAME);
        var conflictingUser = createMockUser(NEW_EMAIL, "Other User");

        when(persistenceGateway.findByEmail(eq(CURRENT_EMAIL))).thenReturn(Optional.of(existingUser));

        when(persistenceGateway.findByEmail(eq(NEW_EMAIL))).thenReturn(Optional.of(conflictingUser));

        var exception = assertThrows(ValidationException.class, () -> {
            updateUserUseCase.execute(CURRENT_EMAIL, NEW_NAME, NEW_EMAIL);
        });

        assertTrue(exception.getMessage().contains("novo e-mail " + NEW_EMAIL + " já está em uso por OUTRO usuário, não permite."));

        verify(persistenceGateway, times(1)).findByEmail(eq(CURRENT_EMAIL));
        verify(persistenceGateway, times(1)).findByEmail(eq(NEW_EMAIL));
        verify(persistenceGateway, never()).deleteByEmail(any());
        verify(persistenceGateway, never()).save(any());
    }

    // ---------------------- CENÁRIOS DE EXCEÇÃO (Validação de E-mail/Nome) ----------------------

    @Test
    @DisplayName("Should throw ValidationException if new email format is invalid")
    void shouldThrowValidationExceptionIfNewEmailFormatIsInvalid() {

        var existingUser = createMockUser(CURRENT_EMAIL, CURRENT_NAME);
        when(persistenceGateway.findByEmail(eq(CURRENT_EMAIL))).thenReturn(Optional.of(existingUser));

        assertThrows(ValidationException.class, () -> {
            updateUserUseCase.execute(CURRENT_EMAIL, NEW_NAME, INVALID_EMAIL);
        });
        verify(persistenceGateway, times(1)).findByEmail(eq(CURRENT_EMAIL));
        verify(persistenceGateway, never()).deleteByEmail(any());
        verify(persistenceGateway, never()).save(any());
    }
}