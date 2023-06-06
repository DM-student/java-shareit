package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    @Autowired
    BookingService service;
    @GetMapping
    public List<BookingDto> getAll(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                   @RequestParam Optional<String> state) {
        return service.getAll(state, userId);
    }
    @GetMapping("/owner")
    public List<BookingDto> getAllForOwner(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                           @RequestParam Optional<String> state) {
        return service.getAllForOwner(userId, state, userId);
    }
    @PostMapping
    public BookingDto post(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                           @RequestBody BookingDto bookingDto) {
        UserDto usr = new UserDto();
        usr.setId(userId);
        bookingDto.setBooker(usr);
        bookingDto.setStatus(BookingState.WAITING);
        return service.post(bookingDto);
    }

    @PatchMapping("/{id}")
    public BookingDto patch(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                            @RequestBody Optional<BookingDto> bookingDtoOptional,
                            @PathVariable Long id,
                            @RequestParam Optional<Boolean> approved) {
        BookingDto bookingDto = bookingDtoOptional.orElseGet(BookingDto::new);
        bookingDto.setStatus(null); // Чтобы не меняли иными методами.
        bookingDto.setId(id);
        if(approved.isPresent()) {
            if(approved.get()) {
                bookingDto.setStatus(BookingState.APPROVED);
            }
            else {
                bookingDto.setStatus(BookingState.REJECTED);
            }
        }
        return service.update(bookingDto, userId);
    }

    @GetMapping("/{id}")
    public BookingDto getAll(@RequestHeader(name = "X-Sharer-User-Id") Long userId, @PathVariable long id){
        return service.getById(id, userId);
    }
}
