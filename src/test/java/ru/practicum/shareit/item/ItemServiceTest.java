package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.data.*;
import ru.practicum.shareit.item.dto.CommentDtoMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utility.JavaxValidationHandler;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemServiceTest {
    // Main test objects
    ItemService testedClass;
    Item testedItem1;
    ItemDto testedItem1Dto;

    // Other objects for tests

    User someUser1;
    UserDto someUser1Dto;

    // dependencies
    private JavaxValidationHandler validation;
    private ItemDataBaseStorage storage;
    private UserDataBaseStorage userStorage;
    private ItemDtoMapper mapper;
    private CommentDataBaseStorage commentStorage;
    private CommentDtoMapper commentMapper;
    private BookingDataBaseStorage bookingStorage;
    private ItemRequestDataBaseStorage requestStorage;

    @BeforeEach
    void setup() {
        testedClass = new ItemService();

        testedItem1 = new Item(1L, 1L, null, "Штука", "Какая-то там штука.", true);
        testedItem1Dto = new ItemDto(1L, someUser1Dto, null, "Штука", "Какая-то там штука",
                true, null, null, null, true);

        someUser1 = new User(1L, "Some One", "some@mail.com");
        someUser1Dto = new UserDto(1L, "some@mail.com", "Some One", false);

        storage = Mockito.mock(ItemDataBaseStorage.class);
        userStorage = Mockito.mock(UserDataBaseStorage.class);
        mapper = Mockito.mock(ItemDtoMapper.class);
        commentStorage = Mockito.mock(CommentDataBaseStorage.class);
        commentMapper = Mockito.mock(CommentDtoMapper.class);
        bookingStorage = Mockito.mock(BookingDataBaseStorage.class);
        requestStorage = Mockito.mock(ItemRequestDataBaseStorage.class);
        validation = new JavaxValidationHandler();

        ReflectionTestUtils.setField(testedClass, "storage", storage);
        ReflectionTestUtils.setField(testedClass, "userStorage", userStorage);
        ReflectionTestUtils.setField(testedClass, "mapper", mapper);
        ReflectionTestUtils.setField(testedClass, "commentStorage", commentStorage);
        ReflectionTestUtils.setField(testedClass, "commentMapper", commentMapper);
        ReflectionTestUtils.setField(testedClass, "bookingStorage", bookingStorage);
        ReflectionTestUtils.setField(testedClass, "requestStorage", requestStorage);
        ReflectionTestUtils.setField(testedClass, "validation", validation);

        // storage
        Mockito.when(storage.save(testedItem1)).thenReturn(testedItem1);
        Mockito.when(storage.findById(1L)).thenReturn(Optional.of(testedItem1));
        Mockito.when(storage.getById(1L)).thenReturn(testedItem1);
        Mockito.when(storage.findAll()).thenReturn(List.of(testedItem1));

        // userStorage
        Mockito.when(userStorage.existsById(1L)).thenReturn(true);

        // mapper
        Mockito.when(mapper.mapToDto(Mockito.eq(testedItem1), Mockito.anyBoolean(), Mockito.anyLong()))
                .thenReturn(testedItem1Dto);
        Mockito.when(mapper.mapToDto(Mockito.eq(testedItem1), Mockito.anyBoolean()))
                .thenReturn(testedItem1Dto);
        Mockito.when(mapper.mapFromDto(testedItem1Dto)).thenReturn(testedItem1);
    }

    @Test
    void testGetSuccess() {
        assertEquals(testedItem1Dto, testedClass.get(1L, 1L));
    }

    @Test
    void testGetAllSuccess() {
        assertEquals(List.of(testedItem1Dto), testedClass.getAll(1L));
    }

    @Test
    void testGetAllForUserEmpty() {
        Mockito.when(userStorage.existsById(Mockito.anyLong())).thenReturn(true);
        assertEquals(List.of(), testedClass.getAllForUser(2L));
    }

    @Test
    void testGetAllForUserNormal() {
        assertEquals(List.of(testedItem1Dto), testedClass.getAllForUser(1L));
    }

    @Test
    void testUploadSuccess() {
        assertEquals(testedItem1Dto, testedClass.upload(testedItem1Dto));
    }

    @Test
    void testUpdateSuccess() {
        Mockito.when(storage.save(Mockito.any())).thenReturn(testedItem1);
        assertEquals(testedItem1Dto, testedClass.update(testedItem1Dto));
    }
}
