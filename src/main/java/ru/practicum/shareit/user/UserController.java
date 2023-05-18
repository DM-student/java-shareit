package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return userService.get(id);
    }
    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }
    @PostMapping
    public UserDto upload(@RequestBody UserDto user) {
        return userService.upload(user);
    }
    @PatchMapping("/{id}")
    public UserDto update(@PathVariable long id, @RequestBody UserDto user) {
        user.setId(id);
        return userService.update(user);
    }
    @DeleteMapping("/{id}")
    public UserDto delete(@PathVariable long id) {
        return userService.delete(id);
    }
}
