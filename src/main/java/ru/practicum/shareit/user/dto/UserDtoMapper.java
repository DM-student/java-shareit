package ru.practicum.shareit.user.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.data.Storage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Component
public class UserDtoMapper {
    @Autowired
    private Storage<Item> items;
    @Lazy
    private ItemDtoMapper itemMapper;

    public UserDto mapToDto(User user, boolean fullData)
    {
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getName(), fullData);
        if(fullData && user.getId() != null) {
            for(long i : user.getItemsIds()) {
                ItemDto item = itemMapper.mapToDto(items.get(i), false);
                userDto.getItems().add(item);
            }
            return userDto;
        }
        return userDto;
    }
    public User mapFromDto(UserDto user) {
        User newUser = new User(user.getId(), user.getName(), user.getEmail());
        for(ItemDto item : user.getItems()) {
            newUser.getItemsIds().add(item.getId());
        }
        return newUser;
    }
}
