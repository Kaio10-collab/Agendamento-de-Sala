package com.send.agendamentosala.application.user;

import message.agendamentosala.application.usecase.user.ReadUserUseCase;
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
class ReadUserUseCaseTest {

    @Mock
    private UserPersistenceGateway persistenceGateway;

    @InjectMocks
    private ReadUserUseCase readUserUseCase;

    private final String VALID_EMAIL = "valid.user@empresa.com";
    private final String INVALID_EMAIL = "invalid-email";
    private final String TEST_NAME = "Test User";

    private User createMockUser() {
        return new User(TEST_NAME, VALID_EMAIL);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should validate email and return the user when found")
    void shouldValidateEmailAndReturnTheUserWhenFound() {

        var expectedUser = createMockUser();

        when(persistenceGateway.findByEmail(eq(VALID_EMAIL))).thenReturn(Optional.of(expectedUser));

        var result = readUserUseCase.execute(VALID_EMAIL);

        assertNotNull(result);
        assertEquals(VALID_EMAIL, result.email());
        assertEquals(TEST_NAME, result.fullName());

        verify(persistenceGateway, times(1)).findByEmail(eq(VALID_EMAIL));
    }

    // ---------------------- CENÁRIO DE EXCEÇÃO (Usuário não encontrado) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when user is not found by email")
    void shouldThrowValidationExceptionWhenUserIsNotFoundByEmail() {

        when(persistenceGateway.findByEmail(eq(VALID_EMAIL))).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> {
            readUserUseCase.execute(VALID_EMAIL);
        });

        assertTrue(exception.getMessage().contains("Usuário não cadastrado: " + VALID_EMAIL));

        verify(persistenceGateway, times(1)).findByEmail(eq(VALID_EMAIL));
    }

    // ---------------------- CENÁRIO DE EXCEÇÃO (Validação de E-mail) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when email format is invalid")
    void shouldThrowValidationExceptionWhenEmailFormatIsInvalid() {

        assertThrows(ValidationException.class, () -> {
            readUserUseCase.execute(INVALID_EMAIL);
        });
        verify(persistenceGateway, never()).findByEmail(any());
    }
}