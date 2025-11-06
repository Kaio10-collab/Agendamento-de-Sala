package message.agendamentosala.infrastructure.gateway.persistence.room;

import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    Optional<RoomEntity> findByName(RoomName name);
    List<RoomEntity> findByStatus(RoomStatus status);
}