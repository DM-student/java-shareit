package ru.practicum.shareit.gateway.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.user.dto.UserDto;

import javax.validation.constraints.Positive;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    @Autowired
    private UserClient client;

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@Positive @PathVariable Long id) {
        return client.getUser(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return client.getUsers();
    }

    @PostMapping
    public ResponseEntity<Object> upload(@RequestBody UserDto user) {
        return client.postUser(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@Positive @PathVariable long id, @RequestBody UserDto user) {
        return client.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@Positive @PathVariable long id) {
        return client.deleteUser(id);
    }
}
