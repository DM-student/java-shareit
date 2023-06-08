package ru.practicum.shareit.item.dto;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.data.BookingDataBaseStorage;
import ru.practicum.shareit.data.CommentDataBaseStorage;
import ru.practicum.shareit.data.UserDataBaseStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemDtoMapper implements ApplicationContextAware, InitializingBean {
    UserDataBaseStorage userStorage;
    BookingDtoMapper bookingMapper;
    BookingDataBaseStorage bookingStorage;
    CommentDataBaseStorage commentStorage;
    CommentDtoMapper commentMapper;
    UserDtoMapper userMapper;

    public ItemDto mapToDto(Item item, boolean fullData) {
        // Так надо, ибо рекурсивные зависимости...
        commentStorage = context.getBean(CommentDataBaseStorage.class);
        bookingMapper = context.getBean(BookingDtoMapper.class);

        ItemDto itemDto = new ItemDto(item.getId(), null, item.getName(),
                item.getDescription(), item.getAvailable(), null, null, null, fullData);
        if (fullData) {
            if (item.getOwnerId() != null) {
                UserDto user = userMapper.mapToDto(userStorage.findById(item.getOwnerId()).get(), false);
                itemDto.setOwner(user);
            }

            List<CommentDto> comments = commentStorage.findCommentsByItemId(item.getId()).stream()
                    .map(comment -> commentMapper.mapToDto(comment)).collect(Collectors.toList());
            itemDto.setComments(comments);


        }
        return itemDto;
    }

    public ItemDto mapToDto(Item item, boolean fullData, Long userId) {
        ItemDto itemDto = mapToDto(item, fullData);
        if (userId == null) {
            return itemDto;
        }

        if (userId.equals(item.getOwnerId())) {
            for (Booking booking : bookingStorage.getBookingsSortedByDate()) {
                if (booking.getItemId().equals(item.getId()) &&
                        booking.getStart().isAfter(LocalDateTime.now()) &&
                        booking.getState() != BookingState.REJECTED) {
                    itemDto.setNextBooking(bookingMapper.mapToDto(booking, false));
                    break;
                }
            }
            for (Booking booking : bookingStorage.getBookingsSortedByDateReverse()) {
                if (booking.getItemId().equals(item.getId()) &&
                        booking.getStart().isBefore(LocalDateTime.now()) &&
                        booking.getState() != BookingState.REJECTED) {
                    itemDto.setLastBooking(bookingMapper.mapToDto(booking, false));
                    break;
                }
            }
            return itemDto;
        }
        /*
        for (Booking booking : bookingStorage.getBookingsByUserIdSortedByDate(userId)) {
            if (booking.getStart().isAfter(LocalDateTime.now())) {
                itemDto.setNextBooking(bookingMapper.mapToDto(booking, false));
                break;
            }
        }

        for (Booking booking : bookingStorage.getBookingsByUserIdSortedByDateReverse(userId)) {
            if (booking.getStart().isBefore(LocalDateTime.now())) {
                itemDto.setLastBooking(bookingMapper.mapToDto(booking, false));
                break;
            }
        }
        */
        return itemDto;
    }

    public Item mapFromDto(ItemDto itemDto) {
        Item item = new Item(itemDto.getId(), null, itemDto.getName(),
                itemDto.getDescription(), itemDto.getAvailable());
        if (itemDto.getOwner() != null) {
            item.setOwnerId(itemDto.getOwner().getId());
        }

        return item;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        userMapper = context.getBean(UserDtoMapper.class);
        commentMapper = context.getBean(CommentDtoMapper.class);
        bookingStorage = context.getBean(BookingDataBaseStorage.class);
        userStorage = context.getBean(UserDataBaseStorage.class);
    }

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }
}
