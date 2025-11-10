package message.agendamentosala.infrastructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import message.agendamentosala.application.usecase.user.CreateUserUseCase;
import message.agendamentosala.application.usecase.user.DeleteUserUseCase;
import message.agendamentosala.application.usecase.user.ReadUserUseCase;
import message.agendamentosala.application.usecase.user.UpdateUserUseCase;
import message.agendamentosala.domain.model.User;
import message.agendamentosala.infrastructure.controller.request.UserRequest;
import message.agendamentosala.infrastructure.controller.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final ReadUserUseCase readUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    private UserResponse toResponse(User user) {
        return new UserResponse(user.fullName(), user.email());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        User user = createUserUseCase.execute(request.fullName(), request.email());
        return new ResponseEntity<>(toResponse(user), HttpStatus.CREATED);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> findByEmail(@PathVariable String email) {
        User user = readUserUseCase.execute(email);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/{currentEmail}")
    public ResponseEntity<UserResponse> update(@PathVariable String currentEmail,
                                               @Valid @RequestBody UserRequest request) {
        User user = updateUserUseCase.execute(currentEmail, request.fullName(), request.email());
        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> delete(@PathVariable String email) {
        deleteUserUseCase.execute(email);
        return ResponseEntity.noContent().build();
    }
}