package message.agendamentosala.infrastructure.gateway.persistence.room;

import lombok.AllArgsConstructor;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.entity.RoomEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomPersistenceGateway {

    private final RoomRepository repository;

    private Room toDomain(RoomEntity entity) {
        return new Room(entity.getId(), entity.getName(), entity.getStatus());
    }

    private RoomEntity toEntity(Room room) {
        return RoomEntity.builder()
                .id(room.id())
                .name(room.name())
                .status(room.status())
                .build();
    }

    public Room save(Room room) {
        RoomEntity savedEntity = repository.save(toEntity(room));
        return toDomain(savedEntity);
    }

    public Optional<Room> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    public Optional<Room> findByName(RoomName name) {
        return repository.findByName(name).map(this::toDomain);
    }

    public List<Room> findAll() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    public List<Room> findByStatus(RoomStatus status) {
        return repository.findByStatus(status).stream().map(this::toDomain).collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}