package ru.practicum.shareit.server.user.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.server.data.ItemDataBaseStorage;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.item.dto.ItemDtoMapper;

@Component
public class UserDtoMapper {
    @Autowired
    private ItemDataBaseStorage items;
    @Lazy
    private ItemDtoMapper itemMapper;

    public UserDto mapToDto(User user, boolean fullData) {
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getName(), fullData);
        if (fullData && user.getId() != null) {
            for (long i : user.getItemsIds()) {
                ItemDto item = itemMapper.mapToDto(items.findById(i).get(), false);
                userDto.getItems().add(item);
            }
            return userDto;
        }
        return userDto;
    }

    public User mapFromDto(UserDto user) {
        User newUser = new User(user.getId(), user.getName(), user.getEmail());
        for (ItemDto item : user.getItems()) {
            newUser.getItemsIds().add(item.getId());
        }
        return newUser;
    }
}
