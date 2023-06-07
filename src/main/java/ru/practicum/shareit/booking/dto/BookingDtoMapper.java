package ru.practicum.shareit.booking.dto;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.data.ItemDataBaseStorage;
import ru.practicum.shareit.data.UserDataBaseStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;

@Component
public class BookingDtoMapper implements ApplicationContextAware, InitializingBean {
    private UserDataBaseStorage userStorage;
    private ItemDataBaseStorage itemStorage;
    private UserDtoMapper userMapper;
    private ItemDtoMapper itemMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        userMapper = context.getBean(UserDtoMapper.class);
        userStorage = context.getBean(UserDataBaseStorage.class);
        itemStorage = context.getBean(ItemDataBaseStorage.class);
    }

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }

    public BookingDto mapToDto(Booking booking, boolean fullData) {
        itemMapper = context.getBean(ItemDtoMapper.class);
        BookingDto newBooking = new BookingDto(booking.getId(), null, null, booking.getStart(), booking.getEnd(),
                booking.getState(), null, null, true);


        if (booking.getUserId() != null) {
            UserDto user = userMapper.mapToDto(userStorage.findById(booking.getUserId()).get(), false);
            newBooking.setBookerId(user.getId());
            if (fullData) {
                newBooking.setBooker(user);
            }
        }
        if (booking.getUserId() != null) {
            ItemDto item = itemMapper.mapToDto(itemStorage.findById(booking.getItemId()).get(), false);
            newBooking.setItemId(item.getId());
            if (fullData) {
                newBooking.setItem(item);
            }
        }
        return newBooking;
    }

    public Booking mapFromDto(BookingDto booking) {
        Booking newBooking = new Booking(booking.getId(), null, null,
                booking.getStatus(), booking.getStart(), booking.getEnd());
        if (booking.getBooker() != null) {
            newBooking.setUserId(booking.getBooker().getId());
        }
        if (booking.getItem() != null) {
            newBooking.setItemId(booking.getItem().getId());
        } else {
            newBooking.setItemId(booking.getItemId());
        }
        return newBooking;
    }
}
