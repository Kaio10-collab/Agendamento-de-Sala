package message.agendamentosala.domain.model;

import message.agendamentosala.domain.validator.EmailValidator;
import message.agendamentosala.domain.validator.NameValidator;

public record User(String fullName, String email) {

    public User {
        NameValidator.validate(fullName);
        EmailValidator.validate(email);
    }
}