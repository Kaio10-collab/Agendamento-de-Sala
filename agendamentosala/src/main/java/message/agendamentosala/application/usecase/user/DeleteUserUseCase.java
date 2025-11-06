package message.agendamentosala.application.usecase.user;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.validator.EmailValidator;
import message.agendamentosala.infrastructure.gateway.persistence.UserPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserPersistenceGateway persistenceGateway;

    public void execute(String email) {
        EmailValidator.validate(email);

        if (persistenceGateway.findByEmail(email).isEmpty()) {
            throw new ValidationException("Não foi possível excluir, usuário não encontrado para o e-mail " + email);
        }
        persistenceGateway.deleteByEmail(email);
    }
}