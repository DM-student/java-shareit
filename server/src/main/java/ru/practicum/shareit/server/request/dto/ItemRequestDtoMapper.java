package ru.practicum.shareit.server.request.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.server.data.ItemDataBaseStorage;
import ru.practicum.shareit.server.data.UserDataBaseStorage;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.ItemDtoMapper;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.dto.UserDtoMapper;

import java.util.HashSet;

@Component
public class ItemRequestDtoMapper {
    @Autowired
    private ItemDataBaseStorage items;
    @Autowired
    private ItemDtoMapper itemMapper;

    @Autowired
    private UserDataBaseStorage userStorage;

    @Autowired
    private UserDtoMapper userMapper;

    public ItemRequestDto mapToDto(ItemRequest request, boolean fullData) {
        ItemRequestDto requestDto = new ItemRequestDto(request.getId(), null, new HashSet<>(), request.getDescription(), request.getCreated());
        if (fullData && requestDto.getId() != null) {
            if (request.getUserId() != null) {
                UserDto user = userMapper.mapToDto(userStorage.getById(request.getUserId()), false);
                requestDto.setUser(user);
            }

            for (long i : request.getItemsIds()) {
                ItemDto item = itemMapper.mapToDto(items.findById(i).get(), false);
                requestDto.getItems().add(item);
            }
            return requestDto;
        }
        return requestDto;
    }

    public ItemRequest mapFromDto(ItemRequestDto requestDto) {
        ItemRequest newRequest = new ItemRequest(requestDto.getId(), null, new HashSet<>(),
                requestDto.getDescription(), requestDto.getCreated());
        for (ItemDto item : requestDto.getItems()) {
            newRequest.getItemsIds().add(item.getId());
        }
        if (requestDto.getUser() != null) {
            newRequest.setUserId(requestDto.getUser().getId());
        }
        return newRequest;
    }
}
