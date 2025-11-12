package message.agendamentosala.infrastructure.gateway.persistence.reservation;

import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.domain.model.RoomStatus;
import message.agendamentosala.infrastructure.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    Optional<ReservationEntity> findByRoomNameAndStatus(RoomName roomName, RoomStatus status);
    List<ReservationEntity> findByUserEmailAndStatusIn(String userEmail, List<RoomStatus> statuses);

    @Query("SELECT r FROM reservation r WHERE r.roomName = :roomName AND r.status IN :statuses " +
            "AND (" +
            "(r.startDateTime < :endDateTime AND r.endDateTime > :startDateTime)" +
            ")")
    List<ReservationEntity> findConflictingReservations(
            @Param("roomName") RoomName roomName,
            @Param("statuses") List<RoomStatus> statuses,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}