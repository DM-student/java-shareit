package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.data.Storage;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utility.JavaxValidationHandler;
import ru.practicum.shareit.utility.exceptions.ShareItValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private JavaxValidationHandler validation;
    @Autowired
    private Storage<User> storage;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserDtoMapper mapper;

    public UserDto get(long id) {
        User user = storage.get(id);
        return mapper.mapToDto(user, true);
    }

    public List<UserDto> getAll() {
        return storage.getAll().stream().map(user -> mapper.mapToDto(user, true)).collect(Collectors.toList());
    }

    public UserDto upload(UserDto user) {
        User newUser = mapper.mapFromDto(user);
        if (!validation.validate(newUser)) {
            throw new ShareItValidationException("Пользователь не прошёл валидацию.",
                    validation.validateFull(newUser));
        }
        return mapper.mapToDto(storage.create(newUser), true);
    }

    public UserDto update(UserDto user) {
        User newUser = mapper.mapFromDto(user);
        User oldUser = storage.get(user.getId());
        oldUser.mergeFrom(newUser);
        if (!validation.validate(oldUser)) {
            throw new ShareItValidationException("Пользователь не прошёл валидацию.",
                    validation.validateFull(oldUser));
        }
        return mapper.mapToDto(storage.update(oldUser), true);
    }

    public UserDto delete(long id) {
        for(long item : storage.get(id).getItemsIds()) {
            itemService.delete(item);
        }
        return mapper.mapToDto(storage.delete(id), true);
    }
}
