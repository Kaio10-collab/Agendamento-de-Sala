package message.agendamentosala.domain.validator;

import message.agendamentosala.domain.exception.ValidationException;

public class NameValidator {

    public static void validate(String name) {

        if (name == null || name.isBlank()) {
            throw new ValidationException("O nome não pode ser nulo ou vazio.");
        }

        if (!name.matches("^[a-zA-Z\\s'-]+$")) {
            throw new ValidationException("O nome não pode conter números ou caracteres especiais.");
        }
    }
}