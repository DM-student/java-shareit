package ru.practicum.shareit.booking;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.data.BookingDataBaseStorage;
import ru.practicum.shareit.data.ItemDataBaseStorage;
import ru.practicum.shareit.data.UserDataBaseStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.utility.JavaxValidationHandler;
import ru.practicum.shareit.utility.exceptions.ShareItNotFoundException;
import ru.practicum.shareit.utility.exceptions.ShareItProvidedDataException;
import ru.practicum.shareit.utility.exceptions.ShareItValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService implements ApplicationContextAware, InitializingBean {
    BookingDataBaseStorage bookingStorage;
    ItemDataBaseStorage itemStorage;
    UserDataBaseStorage userStorage;
    JavaxValidationHandler validation;
    BookingDtoMapper bookingMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        bookingStorage = context.getBean(BookingDataBaseStorage.class);
        userStorage = context.getBean(UserDataBaseStorage.class);
        itemStorage = context.getBean(ItemDataBaseStorage.class);
        bookingMapper = context.getBean(BookingDtoMapper.class);
        validation = context.getBean(JavaxValidationHandler.class);

    }

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }

    public BookingDto post(BookingDto newBooking) {
        Booking booking = bookingMapper.mapFromDto(newBooking);
        booking.setId(null);
        validateInService(booking);
        Booking output = bookingStorage.save(booking);
        return bookingMapper.mapToDto(output, true);
    }

    public BookingDto update(BookingDto newBooking, Long updatingUser) {
        if (!bookingStorage.existsById(newBooking.getId())) {
            throw new ShareItProvidedDataException("Букинг не найден.", newBooking);
        }
        Booking booking = bookingMapper.mapFromDto(newBooking);
        Booking bookingToUpdate = bookingStorage.getById(newBooking.getId()).getClearCopy();

        if (bookingToUpdate.getState() == BookingState.APPROVED) {
            throw new ShareItProvidedDataException("Букинг уже утверждён.", newBooking);
        }

        bookingToUpdate.mergeFrom(booking);
        validateInService(bookingToUpdate);
        applyBookingAndCheckOwner(bookingToUpdate, updatingUser);
        return bookingMapper.mapToDto(bookingStorage.save(bookingToUpdate), true);
    }

    public List<BookingDto> getAll(Optional<String> state, long userId) {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + userId);
        }

        List<BookingDto> out = bookingStorage.getBookingsSortedByDateReverse().stream()
                .filter(booking -> booking.getUserId() == userId)
                .map(booking -> bookingMapper.mapToDto(booking, true))
                .collect(Collectors.toList());
        if (state.isEmpty() || state.get().equals("ALL")) {
            return out;
        }
        switch (state.get()) {
            case "FUTURE":
                return out.stream().filter(booking -> booking.getStart().isAfter(LocalDateTime.now())).collect(Collectors.toList());
            case "PAST":
                return out.stream().filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())).collect(Collectors.toList());
            case "CURRENT":
                return out.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case "WAITING":
                return out.stream().filter(booking -> booking.getStatus() == BookingState.WAITING).collect(Collectors.toList());
            case "APPROVED":
                return out.stream().filter(booking -> booking.getStatus() == BookingState.APPROVED).collect(Collectors.toList());
            case "REJECTED":
                return out.stream().filter(booking -> booking.getStatus() == BookingState.REJECTED).collect(Collectors.toList());
            default:
                throw new ShareItProvidedDataException("Unknown state: UNSUPPORTED_STATUS", state);
        }
    }

    public List<BookingDto> getAllForOwner(long owner, Optional<String> state, long userId) {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + userId);
        }

        List<BookingDto> out = bookingStorage.getBookingsSortedByDateReverse().stream()
                .filter(booking -> isItOwner(booking.getId(), userId))
                .filter(booking -> itemStorage.getById(booking.getItemId()).getOwnerId() == owner)
                .map(booking -> bookingMapper.mapToDto(booking, true))
                .collect(Collectors.toList());
        if (state.isEmpty() || state.get().equals("ALL")) {
            return out;
        }
        switch (state.get()) {
            case "FUTURE":
                return out.stream().filter(booking -> booking.getStart().isAfter(LocalDateTime.now())).collect(Collectors.toList());
            case "PAST":
                return out.stream().filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())).collect(Collectors.toList());
            case "CURRENT":
                return out.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case "WAITING":
                return out.stream().filter(booking -> booking.getStatus() == BookingState.WAITING).collect(Collectors.toList());
            case "APPROVED":
                return out.stream().filter(booking -> booking.getStatus() == BookingState.APPROVED).collect(Collectors.toList());
            case "REJECTED":
                return out.stream().filter(booking -> booking.getStatus() == BookingState.REJECTED).collect(Collectors.toList());
            default:
                throw new ShareItProvidedDataException("Unknown state: UNSUPPORTED_STATUS", state);
        }


    }

    private void applyBookingAndCheckOwner(Booking booking, Long updatingUser) {
        Item item = itemStorage.getById(booking.getItemId());
        if (Objects.equals(updatingUser, booking.getUserId())) {
            if (booking.getState() == BookingState.APPROVED) {
                // 404 просто потому, что так захотелось составителю постмана...
                throw new ShareItNotFoundException("Изменить на *принятый* букинг может только владелец предмета.", booking);
            }
            if (booking.getState() == BookingState.REJECTED) {
                booking.setState(BookingState.CANCELED);
            }
        } else if (!Objects.equals(item.getOwnerId(), updatingUser)) {
            throw new ShareItValidationException("Изменить букинг может только владелец букинка " +
                    "или объявленного предмета!", booking);
        }
    }

    public BookingDto getById(long id, long userId) {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + userId);
        }

        Optional<Booking> bookingOptional = bookingStorage.findById(id);
        if (bookingOptional.isEmpty()) {
            throw new ShareItNotFoundException("Букинг не найден.", id);
        }
        if (!isItOwner(id, userId)) {
            throw new ShareItNotFoundException("Данный объект не найден среди доступных " +
                    "для предоставленного пользователя.", id);
        }
        return bookingMapper.mapToDto(bookingOptional.get(), true);
    }

    private void validateInService(Booking booking) {
        if (!userStorage.existsById(booking.getUserId())) {
            throw new ShareItNotFoundException("Пользователь не найден.", booking);
        }
        if (!validation.validate(booking)) {
            throw new ShareItValidationException("Букинг не прошёл валидацию.", validation.validateFull(booking));
        }
        if (!itemStorage.existsById(booking.getItemId())) {
            throw new ShareItNotFoundException("Предмет не найден.", booking);
        }
        if (!itemStorage.getById(booking.getItemId()).getAvailable()) {
            throw new ShareItProvidedDataException("Предмет не доступен.", booking);
        }
        if (booking.getStart().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(booking.getStart())
                || booking.getStart().isEqual(booking.getEnd())) {
            throw new ShareItProvidedDataException("Время бронирования указано неверно.", booking);
        }
        if (Objects.equals(booking.getUserId(), itemStorage.getById(booking.getItemId()).getOwnerId())) {
            throw new ShareItNotFoundException("Владелец не может арендовать свою вещь", booking); // 404 ибо постман.
        }
    }

    private boolean isItOwner(long bookingId, long userId) {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователь не найден.", userId);
        }

        Booking booking = bookingStorage.getById(bookingId);
        if (booking.getUserId() == userId) {
            return true;
        }
        Item item = itemStorage.getById(booking.getItemId());
        return item.getOwnerId() == userId;
    }
}
