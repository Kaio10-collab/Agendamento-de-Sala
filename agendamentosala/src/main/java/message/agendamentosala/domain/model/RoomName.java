package message.agendamentosala.domain.model;

import lombok.Getter;

@Getter
public enum RoomName {
    HULK(5),
    THOR(10),
    LOKI(15),
    DR_STRANGE(20),
    CAP_MARVEL(25);

    private final int capacity;

    RoomName(int capacity) {
        this.capacity = capacity;
    }
}
