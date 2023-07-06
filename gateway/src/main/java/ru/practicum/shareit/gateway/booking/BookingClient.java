package ru.practicum.shareit.gateway.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;
import ru.practicum.shareit.gateway.utility.BaseClient;
import ru.practicum.shareit.gateway.utility.exceptions.ShareItProvidedDataException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postBooking(BookingDto body, Long userId) {
        if (body.getItemId() == null) {
            throw new ShareItProvidedDataException("ID предмета должен быть указан.", body);
        }
        if (body.getStart() == null) {
            throw new ShareItProvidedDataException("Начало аренды должно быть указано.", body);
        }
        if (body.getEnd() == null) {
            throw new ShareItProvidedDataException("Конец аренды должен быть указан.", body);
        }

        validateBooking(body);
        return post("", userId, body);
    }

    public ResponseEntity<Object> updateBooking(long bookingId, BookingDto body, Long userId, Boolean approved) {
        if (body != null) {
            validateBooking(body);
        }
        if (approved == null) {
            return patch("/{id}", userId, Map.of("id", bookingId), body);
        }
        return patch("/{id}?approved={approved}", userId, Map.of("id", bookingId, "approved", approved), body);
    }

    public ResponseEntity<Object> getBooking(long userId, long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllBookings(long userId, int from, int size, String state) {
        Map<String, Object> parameters =
                Map.of("from", from,
                        "size", size,
                        "state", state);
        switch (state) {
            case "ALL":
            case "FUTURE":
            case "PAST":
            case "CURRENT":
            case "WAITING":
            case "APPROVED":
            case "REJECTED":
                return get("?state={state}&from={from}&size={size}", userId, parameters);
            default:
                throw new ShareItProvidedDataException("Unknown state: UNSUPPORTED_STATUS", state);
        }
    }

    public ResponseEntity<Object> getAllBookingsForOwner(long userId, int from, int size, String state) {
        Map<String, Object> parameters =
                Map.of("from", from,
                        "size", size,
                        "state", state);
        switch (state) {
            case "ALL":
            case "FUTURE":
            case "PAST":
            case "CURRENT":
            case "WAITING":
            case "APPROVED":
            case "REJECTED":
                return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
            default:
                throw new ShareItProvidedDataException("Unknown state: UNSUPPORTED_STATUS", state);
        }
    }

    // Почему так? А потому что иначе аннотационные валидаторы
    // будут с ума сходить от null'ов, которые могут быть отосланы.
    private void validateBooking(BookingDto booking) {
        if (booking.getId() != null && booking.getId() <= 0) {
            throw new ShareItProvidedDataException("ID должен быть больше нуля.", booking);
        }
        if (booking.getItemId() != null && booking.getItemId() <= 0) {
            throw new ShareItProvidedDataException("ID предмета должен быть больше нуля.", booking);
        }
        if (booking.getBookerId() != null && booking.getBookerId() <= 0) {
            throw new ShareItProvidedDataException("ID арендатора должен быть больше нуля.", booking);
        }
        if (booking.getStart() != null && booking.getStart().isBefore(LocalDateTime.now())) {
            throw new ShareItProvidedDataException("Начало аренды не должно быть в прошлом.", booking);
        }
        if (booking.getEnd() != null && booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ShareItProvidedDataException("Конец аренды не должен быть в прошлом.", booking);
        }
        if (booking.getEnd() != null && booking.getStart() != null && booking.getEnd().isBefore(booking.getStart())) {
            throw new ShareItProvidedDataException("Конец аренды не должен быть раньше начала.", booking);
        }
        if (booking.getEnd() != null && booking.getStart() != null && booking.getEnd().isBefore(booking.getStart())) {
            throw new ShareItProvidedDataException("Конец аренды не должен быть раньше начала.", booking);
        }
        if (booking.getItem() != null) {
            if (booking.getItem().getId() != null) {
                if (booking.getItem().getId() <= 0) {
                    throw new ShareItProvidedDataException("ID предмета должен быть больше нуля.", booking);
                }
            }
        }
        if (booking.getBooker() != null && booking.getBooker().getId() != null && booking.getBooker().getId() <= 0) {
            throw new ShareItProvidedDataException("ID арендатора должен быть больше нуля.", booking);
        }
    }
}