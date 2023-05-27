package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.data.UserDataBaseStorage;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utility.JavaxValidationHandler;
import ru.practicum.shareit.utility.exceptions.ShareItNotFoundException;
import ru.practicum.shareit.utility.exceptions.ShareItValidationException;

import javax.persistence.EntityNotFoundException;
import java.util.List;
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
        User user;
        try {
            user = storage.getById(id);
        }
        catch (EntityNotFoundException e) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + id);
        }
        return mapper.mapToDto(user, true);
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
            userToUpdate = storage.getById(newUser.getId());
        }
        catch (EntityNotFoundException e) {
            throw new ShareItNotFoundException("Пользователь не найден.", user);
        }
        long oldId = userToUpdate.getId();
        userToUpdate.mergeFrom(newUser);

        if (!validation.validate(userToUpdate)) {
            throw new ShareItValidationException("Пользователь не прошёл валидацию.",
                    validation.validateFull(userToUpdate));
        }
        return mapper.mapToDto(storage.save(userToUpdate), true);
    }

    public UserDto delete(long id) {
        UserDto deletedUser = get(id);
        storage.deleteById(id);
        return deletedUser;
    }
}
