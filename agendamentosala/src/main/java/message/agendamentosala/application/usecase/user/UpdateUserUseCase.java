package message.agendamentosala.application.usecase.user;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.domain.exception.ValidationException;
import message.agendamentosala.domain.model.User;
import message.agendamentosala.domain.validator.EmailValidator;
import message.agendamentosala.domain.validator.NameValidator;
import message.agendamentosala.infrastructure.gateway.persistence.user.UserPersistenceGateway;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserPersistenceGateway persistenceGateway;

    public User execute(String currentEmail, String newFullName, String newEmail) {

       persistenceGateway.findByEmail(currentEmail)
                .orElseThrow(() -> new ValidationException("Não foi possível atualizar, usuário não encontrado para o e-mail: " + currentEmail));

        NameValidator.validate(newFullName);
        EmailValidator.validate(newEmail);

        if (!currentEmail.equalsIgnoreCase(newEmail) && persistenceGateway.findByEmail(newEmail).isPresent()) {
            throw new ValidationException("novo e-mail " + newEmail + " já está em uso por OUTRO usuário, não permite.");
        }

        User updatedUser = new User(newFullName, newEmail);

        if (!currentEmail.equalsIgnoreCase(newEmail)) {
            persistenceGateway.deleteByEmail(currentEmail);
        }
        return persistenceGateway.save(updatedUser);
    }
}