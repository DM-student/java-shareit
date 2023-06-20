package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utility.exceptions.ShareItProvidedDataException;

import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    @Autowired
    BookingService service;

    @PostMapping
    public BookingDto post(@Positive @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                           @RequestBody BookingDto bookingDto) {
        UserDto usr = new UserDto();
        usr.setId(userId);
        bookingDto.setBooker(usr);
        bookingDto.setStatus(BookingState.WAITING);
        return service.post(bookingDto);
    }

    @PatchMapping("/{id}")
    public BookingDto update(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                             @RequestBody Optional<BookingDto> bookingDtoOptional,
                             @Positive @PathVariable Long id,
                             @RequestParam Optional<Boolean> approved) {
        BookingDto bookingDto = bookingDtoOptional.orElseGet(BookingDto::new);
        bookingDto.setStatus(null); // Чтобы не меняли иными методами.
        bookingDto.setId(id);
        approved.ifPresent(value -> {
            if (value) {
                bookingDto.setStatus(BookingState.APPROVED);
            } else {
                bookingDto.setStatus(BookingState.REJECTED);
            }
        });
        return service.update(bookingDto, userId);
    }

    @GetMapping("/{id}")
    public BookingDto getById(@RequestHeader(name = "X-Sharer-User-Id") Long userId, @PathVariable long id) {
        return service.getById(id, userId);
    }

    @GetMapping
    public List<BookingDto> getAll(@Positive @RequestHeader(name = "X-Sharer-User-Id") long userId,
                                   @RequestParam(required = false, defaultValue = "0") Integer from,
                                   @Positive @RequestParam(required = false, defaultValue = "" + Integer.MAX_VALUE) Integer size,
                                   @RequestParam(required = false, defaultValue = "ALL") String state) {


        // Как оказалось, @Positive - не работает... Буду разбираться, в чём проблема...
        if (from < 0 || size <= 0) {
            throw new ShareItProvidedDataException("Не верные данные для вывода страницы были введены.", "from=" + from + ",size=" + size);
        }
        return service.getAll(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllForOwner(@Positive @RequestHeader(name = "X-Sharer-User-Id") long userId,
                                           @RequestParam(required = false, defaultValue = "0") Integer from,
                                           @Positive @RequestParam(required = false, defaultValue = "" + Integer.MAX_VALUE) Integer size,
                                           @RequestParam(required = false, defaultValue = "ALL") String state) {


        // Как оказалось, @Positive - не работает... Буду разбираться, в чём проблема...
        if (from < 0 || size <= 0) {
            throw new ShareItProvidedDataException("Не верные данные для вывода страницы были введены.", "from=" + from + ",size=" + size);
        }
        return service.getAllForOwner(userId, state, userId, from, size);
    }
}
