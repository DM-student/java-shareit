package ru.practicum.shareit.server.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.data.UserDataBaseStorage;
import ru.practicum.shareit.server.user.dto.UserDtoMapper;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.utility.exceptions.ShareItConflictException;
import ru.practicum.shareit.server.utility.exceptions.ShareItNotFoundException;
import ru.practicum.shareit.server.utility.exceptions.ShareItValidationException;
import ru.practicum.shareit.server.item.ItemService;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.utility.JavaxValidationHandler;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private JavaxValidationHandler validation;
    @Autowired
    private UserDataBaseStorage storage;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserDtoMapper mapper;

    public UserDto get(long id) {
        Optional<User> userOptional = storage.findById(id);

        if (userOptional.isEmpty()) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + id);
        }
        return mapper.mapToDto(userOptional.get(), true);
    }

    public List<UserDto> getAll() {
        return storage.findAll().stream().map(user -> mapper.mapToDto(user, true)).collect(Collectors.toList());
    }

    public UserDto upload(UserDto user) {
        User newUser = mapper.mapFromDto(user);
        if (!validation.validate(newUser)) {
            throw new ShareItValidationException("Пользователь не прошёл валидацию.",
                    validation.validateFull(newUser));
        }
        user.setId(null);
        return mapper.mapToDto(storage.save(newUser), true);
    }

    public UserDto update(UserDto user) {
        User newUser = mapper.mapFromDto(user);
        User userToUpdate;

        try {
            userToUpdate = storage.getById(newUser.getId()).getClearCopy();
        } catch (EntityNotFoundException e) {
            throw new ShareItNotFoundException("Пользователь не найден.", user);
        }
        long oldId = userToUpdate.getId();
        userToUpdate.mergeFrom(newUser);

        if (!validation.validate(userToUpdate)) {
            throw new ShareItValidationException("Пользователь не прошёл валидацию.",
                    validation.validateFull(userToUpdate).toString() + "\"data\":" + userToUpdate);
        }
        if (storage.findOtherUserWithSameEmail(userToUpdate.getEmail(), userToUpdate.getId()) != null) {
            throw new ShareItConflictException("Пользователь с таким адресом электронной почты уже есть.", userToUpdate);
        }
        return mapper.mapToDto(storage.save(userToUpdate), true);
    }

    public UserDto delete(long id) {
        UserDto deletedUser = get(id);
        storage.deleteById(id);
        return deletedUser;
    }
}
