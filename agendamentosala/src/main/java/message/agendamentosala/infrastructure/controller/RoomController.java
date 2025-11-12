package message.agendamentosala.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import message.agendamentosala.application.usecase.room.CreateRoomUseCase;
import message.agendamentosala.application.usecase.room.DeleteRoomUseCase;
import message.agendamentosala.application.usecase.room.ReadRoomUseCase;
import message.agendamentosala.application.usecase.room.UpdateRoomUseCase;
import message.agendamentosala.domain.model.Room;
import message.agendamentosala.domain.model.RoomName;
import message.agendamentosala.infrastructure.controller.response.RoomResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final CreateRoomUseCase createRoomUseCase;
    private final ReadRoomUseCase readRoomUseCase;
    private final UpdateRoomUseCase updateRoomUseCase;
    private final DeleteRoomUseCase deleteRoomUseCase;

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(room.id(), room.name(), room.getCapacity(), room.status());
    }

    @PostMapping("/{roomName}")
    public ResponseEntity<RoomResponse> create(@PathVariable RoomName roomName) {
        Room room = createRoomUseCase.execute(roomName);
        return new ResponseEntity<>(toResponse(room), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> findAll() {
        List<RoomResponse> response = readRoomUseCase.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/{newName}")
    public ResponseEntity<RoomResponse> update(@PathVariable Long id, @PathVariable RoomName newName) {
        Room room = updateRoomUseCase.execute(id, newName);
        return ResponseEntity.ok(toResponse(room));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRoomUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}