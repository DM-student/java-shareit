package ru.practicum.shareit.server.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.server.item.dto.ItemDto;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
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
