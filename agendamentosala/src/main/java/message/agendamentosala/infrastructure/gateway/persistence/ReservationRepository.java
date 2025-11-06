package message.agendamentosala.infrastructure.gateway.persistence;

import message.agendamentosala.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<UserEntity, Long> {
}
