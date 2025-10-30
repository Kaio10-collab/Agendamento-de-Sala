package message.agendamentosala.infrastructure.gateway.persistence;

import lombok.AllArgsConstructor;
import message.agendamentosala.domain.entity.ReservationEntity;
import message.agendamentosala.domain.model.Reservation;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ReservationPersistenceGateway {

    private final ReservationRepository repository;

    public Reservation save(Reservation reservation) {
        ReservationEntity entity = ReservationEntity.builder()
                .id(reservation.id())
                .fullName(reservation.fullName())
                .roomName(reservation.roomName())
                .startDateTime(reservation.startDateTime())
                .endDateTime(reservation.endDateTime())
                .status(reservation.status())
                .build();

        ReservationEntity savedEntity = repository.save(entity);

        return toDomain(savedEntity);
    }

    public Optional<Reservation> findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    private Reservation toDomain(ReservationEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getFullName(),
                entity.getRoomName(),
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getStatus()
        );
    }
}
