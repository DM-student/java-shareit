package ru.practicum.shareit.gateway.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;

import javax.validation.constraints.Positive;
import java.util.Optional;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    @Autowired
    BookingClient client;

    @PostMapping
    public ResponseEntity<Object> post(@Positive @RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                       @RequestBody BookingDto bookingDto) {
        return client.postBooking(bookingDto, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                         @RequestBody Optional<BookingDto> bookingDtoOptional,
                                         @Positive @PathVariable Long id,
                                         @RequestParam Optional<Boolean> approved) {
        return client.updateBooking(id, bookingDtoOptional.orElse(null), userId, approved.orElse(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader(name = "X-Sharer-User-Id") Long userId, @PathVariable long id) {
        return client.getBooking(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@Positive @RequestHeader(name = "X-Sharer-User-Id") long userId,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @Positive @RequestParam(defaultValue = "" + Integer.MAX_VALUE) Integer size,
                                         @RequestParam(defaultValue = "ALL") String state) {


        return client.getAllBookings(userId, from, size, state);

    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllForOwner(@Positive @RequestHeader(name = "X-Sharer-User-Id") long userId,
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(defaultValue = "" + Integer.MAX_VALUE) Integer size,
                                                 @RequestParam(defaultValue = "ALL") String state) {


        return client.getAllBookingsForOwner(userId, from, size, state);
    }
}
