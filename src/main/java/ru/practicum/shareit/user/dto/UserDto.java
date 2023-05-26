package ru.practicum.shareit.user.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private Set<ItemDto> items = new HashSet<>();

    private boolean fullData = true; // Нужно для nesting'а ДТО.

    public UserDto(Long id, String email, String name, boolean fullData) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.fullData = fullData;
    }
}
