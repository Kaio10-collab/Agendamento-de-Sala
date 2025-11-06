package message.agendamentosala.application.usecase.user;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.User;
import message.agendamentosala.domain.validator.EmailValidator;
import message.agendamentosala.infrastructure.gateway.persistence.UserPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadUserUseCase {

    private final UserPersistenceGateway persistenceGateway;

    public User execute(String email) {

        EmailValidator.validate(email);

        return persistenceGateway.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Usuário não cadastrado: " + email));
    }
}