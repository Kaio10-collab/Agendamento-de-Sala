package message.agendamentosala.infrastructure.gateway.persistence;

import lombok.AllArgsConstructor;
import message.agendamentosala.domain.model.User;
import message.agendamentosala.infrastructure.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserPersistenceGateway {

    private final UserRepository repository;

    private User toDomain(UserEntity entity) {
        return new User(entity.getFullName(), entity.getEmail());
    }

    private UserEntity toEntity(User user) {
        return UserEntity.builder()
                .fullName(user.fullName())
                .email(user.email())
                .build();
    }

    public Optional<User> findByEmail(String email) {
        return repository.findById(email).map(this::toDomain);
    }

    public User save(User user) {
        UserEntity savedEntity = repository.save(toEntity(user));
        return toDomain(savedEntity);
    }

    public void deleteByEmail(String email) {
        repository.deleteById(email);
    }
}