package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.data.ItemRequestDataBaseStorage;
import ru.practicum.shareit.data.UserDataBaseStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.utility.JavaxValidationHandler;
import ru.practicum.shareit.utility.exceptions.ShareItNotFoundException;
import ru.practicum.shareit.utility.exceptions.ShareItValidationException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemRequestService {
    @Autowired
    private JavaxValidationHandler validation;
    @Autowired
    private ItemRequestDataBaseStorage storage;
    @Autowired
    private ItemRequestDtoMapper mapper;
    @Autowired
    private UserDataBaseStorage userStorage;

    public ItemRequestDto get(long id) {
        Optional<ItemRequest> requestOptional = storage.findById(id);
        if (requestOptional.isEmpty()) {
            throw new ShareItNotFoundException("Запрос не найден.", "request.id = " + id);
        }
        return mapper.mapToDto(requestOptional.get(), true);
    }

    public List<ItemRequestDto> getAllWithPage(long from, int size) {
        Pageable pageable = PageRequest.of((int) from / size, size);
        return storage.getRequestsSortedByDate(pageable).stream()
                .map(itemRequest -> mapper.mapToDto(itemRequest, true)).collect(Collectors.toList());

    }

    public List<ItemRequestDto> getAllWithPageExcludeUser(long from, int size, long id) {
        Pageable pageable = PageRequest.of((int) from / size, size);
        return storage.getRequestsSortedByDateExcludeUser(id, pageable).stream()
                .map(itemRequest -> mapper.mapToDto(itemRequest, true)).collect(Collectors.toList());
    }

    public List<ItemRequestDto> getAllForUser(Long id) {
        if (!userStorage.existsById(id)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + id);
        }
        return storage.getRequestsSortedByDateAndFindByUser(id).stream()
                .map(item -> mapper.mapToDto(item, true)).collect(Collectors.toList());
    }

    public List<ItemRequestDto> getAll() {
        return storage.getRequestsSortedByDate().stream()
                .map(item -> mapper.mapToDto(item, true)).collect(Collectors.toList());
    }

    public ItemRequestDto post(ItemRequestDto requestDto) {
        ItemRequest request = mapper.mapFromDto(requestDto);
        request.setCreated(LocalDateTime.now());
        if (!validation.validate(request)) {
            throw new ShareItValidationException("Предмет не прошёл валидацию.",
                    validation.validateFull(request));
        }
        if (userStorage.findById(request.getUserId()).isEmpty()) {
            throw new ShareItNotFoundException("Был указан несуществующий составитель.", request);
        }
        request.setId(null);
        return mapper.mapToDto(storage.save(request), true);
    }

    public ItemRequestDto update(ItemRequestDto itemRequest) {
        ItemRequest newItemRequest = mapper.mapFromDto(itemRequest);
        ItemRequest itemRequestToUpdate;

        try {
            itemRequestToUpdate = storage.getById(newItemRequest.getId()).getClearCopy();
        } catch (EntityNotFoundException e) {
            throw new ShareItNotFoundException("Запрос не найден.", itemRequest);
        }
        long oldOwnerId = itemRequestToUpdate.getUserId();
        itemRequestToUpdate.mergeFrom(newItemRequest);

        if (!validation.validate(itemRequestToUpdate)) {
            throw new ShareItValidationException("Запрос не прошёл валидацию.",
                    validation.validateFull(itemRequestToUpdate));
        }

        if (!Objects.equals(oldOwnerId, newItemRequest.getUserId())) {
            throw new ShareItNotFoundException("При обновлении предмета указан новый пользователь.", itemRequest);
        }
        return mapper.mapToDto(storage.save(itemRequestToUpdate), true);
    }

    public void validateUser(long id) {
        if (!userStorage.existsById(id)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + id);
        }
    }
}
