package message.agendamentosala.domain.validator;

import message.agendamentosala.domain.exception.ValidationException;

public class EmailValidator {

    public static void validate(String email) {

        if (email == null || email.isBlank()) {
            throw new ValidationException("O e-mail não pode ser nulo ou vazio.");
        }

        if (!email.matches("^[\\w.-]+@([\\w-]+\\.)+[A-Z]{2,4}$")) {
            throw new ValidationException("Formato de e-mail inválido.");
        }
    }
}