package ru.practicum.shareit.server.booking;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingState;
import ru.practicum.shareit.server.data.BookingDataBaseStorage;
import ru.practicum.shareit.server.data.ItemDataBaseStorage;
import ru.practicum.shareit.server.data.UserDataBaseStorage;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.utility.JavaxValidationHandler;
import ru.practicum.shareit.server.utility.exceptions.ShareItNotFoundException;
import ru.practicum.shareit.server.utility.exceptions.ShareItProvidedDataException;
import ru.practicum.shareit.server.utility.exceptions.ShareItValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService implements ApplicationContextAware, InitializingBean {
    private BookingDataBaseStorage bookingStorage;
    private ItemDataBaseStorage itemStorage;
    private UserDataBaseStorage userStorage;
    private JavaxValidationHandler validation;
    private BookingDtoMapper bookingMapper;

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
        Booking postedBooking = bookingStorage.save(booking);
        return bookingMapper.mapToDto(postedBooking, true);
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

    public List<BookingDto> getAll(String state, long userId, long from, long size) {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + userId);
        }

        List<BookingDto> out = bookingStorage.getBookingsSortedByDateReverse().stream()
                .filter(booking -> booking.getUserId() == userId)
                .map(booking -> bookingMapper.mapToDto(booking, true))
                .collect(Collectors.toList());

        List<BookingDto> finalOutput;

        switch (state) {
            case "ALL":
                finalOutput = out;
                break;
            case "FUTURE":
                finalOutput = out.stream().filter(booking -> booking.getStart().isAfter(LocalDateTime.now())).collect(Collectors.toList());
                break;
            case "PAST":
                finalOutput = out.stream().filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())).collect(Collectors.toList());
                break;
            case "CURRENT":
                finalOutput = out.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case "WAITING":
                finalOutput = out.stream().filter(booking -> booking.getStatus() == BookingState.WAITING).collect(Collectors.toList());
                break;
            case "APPROVED":
                finalOutput = out.stream().filter(booking -> booking.getStatus() == BookingState.APPROVED).collect(Collectors.toList());
                break;
            case "REJECTED":
                finalOutput = out.stream().filter(booking -> booking.getStatus() == BookingState.REJECTED).collect(Collectors.toList());
                break;
            default:
                throw new ShareItProvidedDataException("Unknown state: UNSUPPORTED_STATUS", state);
        }
        return listToPageKnockOff(finalOutput, from, size);
    }

    public List<BookingDto> getAllForOwner(long owner, String state, long userId, long from, long size) {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + userId);
        }

        List<BookingDto> out = bookingStorage.getBookingsSortedByDateReverse().stream()
                .filter(booking -> isItOwner(booking.getId(), userId))
                .filter(booking -> itemStorage.getById(booking.getItemId()).getOwnerId() == owner)
                .map(booking -> bookingMapper.mapToDto(booking, true))
                .collect(Collectors.toList());

        List<BookingDto> finalOutput;

        switch (state) {
            case "ALL":
                finalOutput = out;
                break;
            case "FUTURE":
                finalOutput = out.stream().filter(booking -> booking.getStart().isAfter(LocalDateTime.now())).collect(Collectors.toList());
                break;
            case "PAST":
                finalOutput = out.stream().filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())).collect(Collectors.toList());
                break;
            case "CURRENT":
                finalOutput = out.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case "WAITING":
                finalOutput = out.stream().filter(booking -> booking.getStatus() == BookingState.WAITING).collect(Collectors.toList());
                break;
            case "APPROVED":
                finalOutput = out.stream().filter(booking -> booking.getStatus() == BookingState.APPROVED).collect(Collectors.toList());
                break;
            case "REJECTED":
                finalOutput = out.stream().filter(booking -> booking.getStatus() == BookingState.REJECTED).collect(Collectors.toList());
                break;
            default:
                throw new ShareItProvidedDataException("Unknown state: UNSUPPORTED_STATUS", state);
        }
        return listToPageKnockOff(finalOutput, from, size);
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

    // ОЧЕНЬ ВАЖНО!!
    // Почему так? А потому, что у меня уже построена логика на фильтрации в сервисе, я с ума сойду логику
    // переносить на БД, рефакторя всё под ноль...
    private List<BookingDto> listToPageKnockOff(List<BookingDto> list, long from, long size) {
        List<BookingDto> listToReturn = new ArrayList<>();
        if (from >= list.size()) {
            return listToReturn;
        }
        int itemsAdded = 0;
        for (int i = (int) from; i < list.size(); i++) {
            listToReturn.add(list.get(i));
            itemsAdded++;
            if (itemsAdded == size) {
                break;
            }
        }
        return listToReturn;
    }
}
