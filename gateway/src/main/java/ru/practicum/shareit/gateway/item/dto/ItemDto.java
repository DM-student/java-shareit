package ru.practicum.shareit.gateway.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;
import ru.practicum.shareit.gateway.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private UserDto owner;
    private Long requestId;
    private String name;
    private String description;
    private Boolean available;
    private BookingDto nextBooking;
    private BookingDto lastBooking;
    private List<CommentDto> comments = new ArrayList<>();

    private boolean fullData = true; // Нужно для nesting'а ДТО.
}
