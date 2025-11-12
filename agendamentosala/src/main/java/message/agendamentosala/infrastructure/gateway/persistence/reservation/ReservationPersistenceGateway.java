package message.agendamentosala.infrastructure.gateway.persistence.reservation;

import lombok.AllArgsConstructor;
import message.agendamentosala.domain.model.Reservation;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.entity.ReservationEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationPersistenceGateway {

    private final ReservationRepository repository;

    private Reservation toDomain(ReservationEntity entity) {
        return new Reservation(
                entity.getId(), entity.getUserEmail(), entity.getRoomName(), entity.getRequiredPeople(),
                entity.getStartDateTime(), entity.getEndDateTime(), entity.getStatus()
        );
    }

    private ReservationEntity toEntity(Reservation reservation) {
        return ReservationEntity.builder()
                .id(reservation.id())
                .userEmail(reservation.userEmail())
                .roomName(reservation.roomName())
                .requiredPeople(reservation.requiredPeople())
                .startDateTime(reservation.startDateTime())
                .endDateTime(reservation.endDateTime())
                .status(reservation.status())
                .build();
    }

    public Reservation save(Reservation reservation) {
        ReservationEntity savedEntity = repository.save(toEntity(reservation));
        return toDomain(savedEntity);
    }

    public Optional<Reservation> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    public List<Reservation> findActiveReservationsByUserEmail(String userEmail) {
        List<RoomStatus> activeStatuses = List.of(RoomStatus.STAND_BY, RoomStatus.PENDING, RoomStatus.CHECKED_IN);
        return repository.findByUserEmailAndStatusIn(userEmail, activeStatuses)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    public boolean hasConflict(RoomName roomName, LocalDateTime start, LocalDateTime end) {
        List<RoomStatus> conflictingStatuses = List.of(RoomStatus.STAND_BY, RoomStatus.PENDING, RoomStatus.CHECKED_IN);
        return !repository.findConflictingReservations(roomName, conflictingStatuses, start, end).isEmpty();
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}