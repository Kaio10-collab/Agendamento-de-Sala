package com.send.agendamentosala.application.user;

import message.agendamentosala.application.usecase.user.CreateUserUseCase;
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
class CreateUserUseCaseTest {

    @Mock
    private UserPersistenceGateway persistenceGateway;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    private final String TEST_NAME = "Alice Silva";
    private final String TEST_EMAIL = "alice.silva@empresa.com";

    private User createMockUser() {
        return new User(TEST_NAME, TEST_EMAIL);
    }

    // ---------------------- CENÁRIO DE SUCESSO ----------------------

    @Test
    @DisplayName("Should save the new user when email does not exist")
    void shouldSaveTheNewUserWhenEmailDoesNotExist() {

        var mockUser = createMockUser();

        when(persistenceGateway.findByEmail(eq(TEST_EMAIL))).thenReturn(Optional.empty());
        when(persistenceGateway.save(any(User.class))).thenReturn(mockUser);

        var result = createUserUseCase.execute(TEST_NAME, TEST_EMAIL);

        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.email());
        assertEquals(TEST_NAME, result.fullName());

        verify(persistenceGateway, times(1)).findByEmail(eq(TEST_EMAIL));
        verify(persistenceGateway, times(1)).save(any(User.class));
    }

    // ---------------------- CENÁRIO DE EXCEÇÃO (E-mail já existe) ----------------------

    @Test
    @DisplayName("Should throw ValidationException when user with the same email already exists")
    void shouldThrowValidationExceptionWhenUserWithTheSameEmailAlreadyExists() {

        var existingUser = createMockUser();

        when(persistenceGateway.findByEmail(eq(TEST_EMAIL))).thenReturn(Optional.of(existingUser));

        var exception = assertThrows(ValidationException.class, () -> {
            createUserUseCase.execute(TEST_NAME, TEST_EMAIL);
        });

        assertTrue(exception.getMessage().contains("já existe. Não deve salvar o registro."));

        verify(persistenceGateway, times(1)).findByEmail(eq(TEST_EMAIL));
        verify(persistenceGateway, never()).save(any());
    }
}