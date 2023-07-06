package ru.practicum.shareit.gateway.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.server.booking.BookingService;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingState;
import ru.practicum.shareit.server.data.BookingDataBaseStorage;
import ru.practicum.shareit.server.data.ItemDataBaseStorage;
import ru.practicum.shareit.server.data.UserDataBaseStorage;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.utility.JavaxValidationHandler;
import ru.practicum.shareit.server.utility.exceptions.ShareItNotFoundException;
import ru.practicum.shareit.server.utility.exceptions.ShareItValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BookingServiceTest {

    BookingService testedClass;

    Booking booking1TestEntity;
    BookingDto booking1DtoTestEntity;

    BookingDataBaseStorage bookingStorage;
    ItemDataBaseStorage itemStorage;
    UserDataBaseStorage userStorage;
    BookingDtoMapper bookingMapper;
    JavaxValidationHandler validation = new JavaxValidationHandler();


    @BeforeEach
    void beforeEach() {
        testedClass = new BookingService();

        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = LocalDateTime.now().plusMinutes(20);

        // Цифры в названии для удобства...
        UserDto userDto1 = new UserDto(1L, "some@mail.com", "Some One", false);
        User user1 = new User(1L, "Some One", "some@mail.com");
        User user5 = new User(5L, "Other Dude", "Other@mail.com");

        ItemDto itemDto1 = new ItemDto(1L, null, null, "Штука", "Какая-то штука",
                true, null, null, null, false);
        Item item1 = new Item(1L, 5L, null, "Штука", "Какая-то штука", true);

        booking1TestEntity = new Booking(1L, 1L, 1L, BookingState.WAITING, start, end);
        booking1DtoTestEntity = new BookingDto(1L, 1L, 1L, start, end,
                BookingState.WAITING, userDto1, itemDto1, true);

        bookingStorage = Mockito.mock(BookingDataBaseStorage.class);
        itemStorage = Mockito.mock(ItemDataBaseStorage.class);
        userStorage = Mockito.mock(UserDataBaseStorage.class);
        bookingMapper = Mockito.mock(BookingDtoMapper.class);

        ReflectionTestUtils.setField(testedClass, "bookingStorage", bookingStorage);
        ReflectionTestUtils.setField(testedClass, "itemStorage", itemStorage);
        ReflectionTestUtils.setField(testedClass, "userStorage", userStorage);
        ReflectionTestUtils.setField(testedClass, "bookingMapper", bookingMapper);
        ReflectionTestUtils.setField(testedClass, "validation", validation);

        // userStorage
        Mockito.when(userStorage.existsById(Mockito.anyLong())).thenReturn(false);
        Mockito.when(userStorage.existsById(1L)).thenReturn(true);
        Mockito.when(userStorage.existsById(5L)).thenReturn(true);

        // itemStorage
        Mockito.when(itemStorage.existsById(Mockito.anyLong())).thenReturn(false);
        Mockito.when(itemStorage.existsById(1L)).thenReturn(true);
        Mockito.when(itemStorage.getById(1L)).thenReturn(item1);

        // bookingMapper
        Mockito.when(bookingMapper.mapToDto(booking1TestEntity, true)).thenReturn(booking1DtoTestEntity);
        Mockito.when(bookingMapper.mapFromDto(booking1DtoTestEntity)).thenReturn(booking1TestEntity);

        // bookingStorage
        Mockito.when(bookingStorage.existsById(1L)).thenReturn(true);
        Mockito.when(bookingStorage.getById(1L)).thenReturn(booking1TestEntity);
        Mockito.when(bookingStorage.findById(1L)).thenReturn(Optional.of(booking1TestEntity));
        Mockito.when(bookingStorage.getBookingsSortedByDate()).thenReturn(List.of(booking1TestEntity));
        Mockito.when(bookingStorage.getBookingsSortedByDateReverse()).thenReturn(List.of(booking1TestEntity));
        Mockito.when(bookingStorage.save(booking1TestEntity)).thenReturn(booking1TestEntity);
    }

    @Test
    void testPostSuccess() {
        assertEquals(testedClass.post(booking1DtoTestEntity), booking1DtoTestEntity);
    }

    @Test
    void testPostInvalidBooking() {
        booking1DtoTestEntity.setStatus(null);
        booking1TestEntity.setState(null);
        assertThrows(ShareItValidationException.class, () -> testedClass.post(booking1DtoTestEntity));
    }

    @Test
    void testUpdateSuccess() {
        assertEquals(testedClass.update(booking1DtoTestEntity, 1L), booking1DtoTestEntity);
    }

    @Test
    void testFailUpdateByAnotherUser() {
        User user2ForTest = new User(2L, "sdfgsdfg", "vbncgf@mail.com");
        Mockito.when(userStorage.existsById(2L)).thenReturn(true);
        assertThrows(ShareItValidationException.class, () -> testedClass.update(booking1DtoTestEntity, 2L));
    }

    @Test
    void testGetAllSuccess() {
        assertEquals(testedClass.getAll("ALL", 1L, 0L, 20), List.of(booking1DtoTestEntity));
    }

    @Test
    void testGetAllForOwnerSuccess() {
        assertEquals(testedClass.getAllForOwner(5L, "ALL", 5L, 0L, 20), List.of(booking1DtoTestEntity));
    }

    @Test
    void testGetByIdSuccess() {
        assertEquals(testedClass.getById(1L, 1L), booking1DtoTestEntity);
    }

    @Test
    void testWithWrongUser() {
        assertThrows(ShareItNotFoundException.class, () -> testedClass.getById(1L, 999L));
        assertThrows(ShareItNotFoundException.class,
                () -> testedClass.getAllForOwner(5L, "ALL", 999L, 0L, 20));
    }
}
