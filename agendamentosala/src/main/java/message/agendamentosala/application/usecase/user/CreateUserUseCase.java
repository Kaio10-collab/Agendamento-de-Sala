package message.agendamentosala.application.usecase.user;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.User;
import message.agendamentosala.infrastructure.gateway.persistence.user.UserPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserPersistenceGateway persistenceGateway;

    public User execute(String fullName, String email) {

        User newUser = new User(fullName, email);

        if (persistenceGateway.findByEmail(email).isPresent()) {
            throw new ValidationException("Usuário com o e-mail " + email + " já existe. Não deve salvar o registro.");
        }
        return persistenceGateway.save(newUser);
    }
}