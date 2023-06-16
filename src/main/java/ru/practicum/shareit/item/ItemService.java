package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.data.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.utility.JavaxValidationHandler;
import ru.practicum.shareit.utility.exceptions.ShareItNotFoundException;
import ru.practicum.shareit.utility.exceptions.ShareItProvidedDataException;
import ru.practicum.shareit.utility.exceptions.ShareItValidationException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {
    @Autowired
    private JavaxValidationHandler validation;
    @Autowired
    private ItemDataBaseStorage storage;
    @Autowired
    private UserDataBaseStorage userStorage;
    @Autowired
    private ItemDtoMapper mapper;
    @Autowired
    private CommentDataBaseStorage commentStorage;
    @Autowired
    private CommentDtoMapper commentMapper;
    @Autowired
    private BookingDataBaseStorage bookingStorage;
    @Autowired
    private ItemRequestDataBaseStorage requestStorage;

    public ItemDto get(long id, Long userId) {
        Optional<Item> itemOptional = storage.findById(id);
        if (itemOptional.isEmpty()) {
            throw new ShareItNotFoundException("Предмет не найден.", "item.id = " + id);
        }
        return mapper.mapToDto(itemOptional.get(), true, userId);
    }

    public List<ItemDto> getAll(Long userId) {
        return storage.findAll().stream().map(item -> mapper.mapToDto(item, true, userId)).collect(Collectors.toList());
    }

    public List<ItemDto> getAllForUser(Long id) {
        if (!userStorage.existsById(id)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + id);
        }
        return storage.findAll().stream().filter(item -> Objects.equals(item.getOwnerId(), id))
                .map(item -> mapper.mapToDto(item, true, id)).collect(Collectors.toList());
    }

    public ItemDto upload(ItemDto item) {
        Item newItem = mapper.mapFromDto(item);
        if (!validation.validate(newItem)) {
            throw new ShareItValidationException("Предмет не прошёл валидацию.",
                    validation.validateFull(newItem));
        }
        if (!userStorage.existsById(newItem.getOwnerId())) {
            throw new ShareItNotFoundException("Был указан несуществующий владелец.", newItem);
        }
        if (item.getRequestId() != null) if (!requestStorage.existsById(item.getRequestId())) {
            throw new ShareItNotFoundException("Запрос предмета (requestId) не найден.", item);
        }
        item.setId(null);
        return mapper.mapToDto(storage.save(newItem), true);
    }

    public ItemDto update(ItemDto item) {
        Item newItem = mapper.mapFromDto(item);
        Item itemToUpdate;

        try {
            itemToUpdate = storage.getById(newItem.getId()).getClearCopy();
        } catch (EntityNotFoundException e) {
            throw new ShareItNotFoundException("Предмет не найден.", item);
        }
        long oldOwnerId = itemToUpdate.getOwnerId();
        itemToUpdate.mergeFrom(newItem);

        if (!validation.validate(itemToUpdate)) {
            throw new ShareItValidationException("Предмет не прошёл валидацию.",
                    validation.validateFull(itemToUpdate));
        }
        if (item.getRequestId() != null) if (!requestStorage.existsById(item.getRequestId())) {
            throw new ShareItNotFoundException("Запрос предмета (requestId) не найден.", item);
        }

        if (!Objects.equals(oldOwnerId, newItem.getOwnerId())) {
            throw new ShareItNotFoundException("При обновлении предмета указан новый пользователь.", item);
        }
        return mapper.mapToDto(storage.save(itemToUpdate), true);
    }

    public ItemDto delete(long id) {
        ItemDto deletedItem = get(id, null);
        storage.deleteById(id);
        return deletedItem;
    }

    // Я так и не понял, нужен ли мне поиск не доступных штук.
    public List<ItemDto> getSearched(String query) {
        return storage.findAll().stream().filter(item -> {
                    if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                        return true;
                    }
                    return item.getDescription().toLowerCase().contains(query.toLowerCase());
                }).map(item -> mapper.mapToDto(item, true))
                .collect(Collectors.toList());
    }

    public List<ItemDto> getSearchedAvailable(String query) {
        return storage.findAll().stream().filter(item -> {
                    if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                        return item.getAvailable();
                    }
                    if (item.getDescription().toLowerCase().contains(query.toLowerCase())) {
                        return item.getAvailable();
                    }
                    return false;
                }).map(item -> mapper.mapToDto(item, true))
                .collect(Collectors.toList());
    }

    public CommentDto postComment(Comment comment) {
        comment.setCreated(LocalDateTime.now());
        if (!validation.validate(comment)) {
            throw new ShareItValidationException("Комментарий не прошёл валидацию.", validation.validateFull(comment));
        }
        if (!userStorage.existsById(comment.getUserId())) {
            throw new ShareItProvidedDataException("Пользователь не найден.", comment);
        }
        if (!storage.existsById(comment.getItemId())) {
            throw new ShareItProvidedDataException("Предмет не найден.", comment);
        }
        if (bookingStorage.getBookingsByUserIdSortedByDate(comment.getUserId()).stream()
                .noneMatch(booking -> Objects.equals(booking.getItemId(), comment.getItemId()) &&
                        booking.getState() == BookingState.APPROVED &&
                        booking.getStart().isBefore(LocalDateTime.now()))) {
            throw new ShareItProvidedDataException("Пользователь не брал предмет в аренду.", comment);
        }
        return commentMapper.mapToDto(commentStorage.save(comment));
    }
}
