package com.send.agendamentosala.application.user;

import message.agendamentosala.application.usecase.user.DeleteUserUseCase;
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
class DeleteUserUseCaseTest {

    @Mock
    private UserPersistenceGateway persistenceGateway;

    @InjectMocks
    private DeleteUserUseCase deleteUserUseCase;

    private final String VALID_EMAIL = "valid.user@empresa.com";
    private final String INVALID_EMAIL = "invalid-email";
    private final String TEST_NAME = "Test User";

    private User createMockUser() {
        return new User(TEST_NAME, VALID_EMAIL);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should validate email, find user, and proceed with deletion when successful")
    void shouldValidateEmailFindUserAndProceedWithDeletionWhenSuccessful() {
        // Arrange
        User existingUser = createMockUser();

        // 1. Mock: findByEmail retorna Optional.of(existingUser) (usuário encontrado)
        when(persistenceGateway.findByEmail(eq(VALID_EMAIL))).thenReturn(Optional.of(existingUser));

        // 2. Mock: deleteByEmail não faz nada
        doNothing().when(persistenceGateway).deleteByEmail(eq(VALID_EMAIL));

        // Act & Assert
        // Nota: O EmailValidator.validate() é chamado internamente e deve passar para o VALID_EMAIL.
        assertDoesNotThrow(() -> deleteUserUseCase.execute(VALID_EMAIL));

        // Verifica as interações
        verify(persistenceGateway, times(1)).findByEmail(eq(VALID_EMAIL));
        verify(persistenceGateway, times(1)).deleteByEmail(eq(VALID_EMAIL));
    }

    // ---------------------- CENÁRIOS DE EXCEÇÃO (Regras de Negócio) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when user is not found by email")
    void shouldThrowValidationExceptionWhenUserIsNotFoundByEmail() {

        when(persistenceGateway.findByEmail(eq(VALID_EMAIL))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            deleteUserUseCase.execute(VALID_EMAIL);
        });

        assertTrue(exception.getMessage().contains("usuário não encontrado para o e-mail " + VALID_EMAIL));

        verify(persistenceGateway, times(1)).findByEmail(eq(VALID_EMAIL));
        verify(persistenceGateway, never()).deleteByEmail(any());
    }

    // ---------------------- CENÁRIO DE EXCEÇÃO (Validação de E-mail) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when email format is invalid")
    void shouldThrowValidationExceptionWhenEmailFormatIsInvalid() {

        assertThrows(ValidationException.class, () -> {
            deleteUserUseCase.execute(INVALID_EMAIL);
        });

        verify(persistenceGateway, never()).findByEmail(any());
        verify(persistenceGateway, never()).deleteByEmail(any());
    }
}