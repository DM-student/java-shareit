package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.data.Storage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utility.JavaxValidationHandler;
import ru.practicum.shareit.utility.exceptions.NotFoundExceptionForHandler;
import ru.practicum.shareit.utility.exceptions.ValidationExceptionForHandler;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {
    @Autowired
    private JavaxValidationHandler validation;
    @Autowired
    private Storage<Item> storage;
    @Autowired
    private Storage<User> userStorage;
    @Autowired
    private ItemDtoMapper mapper;

    public ItemDto get(long id) {
        Item item = storage.get(id);
        return mapper.mapToDto(item, true);
    }

    public List<ItemDto> getAll() {
        return storage.getAll().stream().map(u -> mapper.mapToDto(u, true)).collect(Collectors.toList());
    }

    public List<ItemDto> getAllForUser(Long id) {
        String args[] = new String[2];
        args[0] = "user";
        args[1] = id.toString();
        return storage.specialGet(args).stream().map(u -> mapper.mapToDto(u, true)).collect(Collectors.toList());
    }

    public ItemDto upload(ItemDto item) {
        Item newItem = mapper.mapFromDto(item);
        if (!validation.validate(newItem)) {
            throw new ValidationExceptionForHandler("Предмет не прошёл валидацию.",
                    validation.validateFull(newItem));
        }
        userStorage.get(item.getOwner().getId());
        return mapper.mapToDto(storage.upload(newItem), true);
    }

    public ItemDto update(ItemDto item) {
        Item newItem = mapper.mapFromDto(item);
        Item oldItem = storage.get(item.getId());
        oldItem.mergeFrom(newItem);

        if (!validation.validate(oldItem)) {
            throw new ValidationExceptionForHandler("Предмет не прошёл валидацию.",
                    validation.validateFull(oldItem));
        }

        if (!Objects.equals(storage.get(item.getId()).getOwnerId(), item.getOwner().getId())) {
            throw new NotFoundExceptionForHandler("При обновлении предмета указан новый пользователь.", item);
        }
        return mapper.mapToDto(storage.update(oldItem), true);
    }

    public ItemDto delete(long id) {
        return mapper.mapToDto(storage.delete(id), true);
    }

    public List<ItemDto> getSearched(String query) {
        String[] args = new String[2];
        args[0] = "search";
        args[1] = query;
        List<ItemDto> output = storage.specialGet(args).stream()
                .map(u -> mapper.mapToDto(u, true)).collect(Collectors.toList());
        log.info("Возвращён список предметов по поисковому запросу \"{}\", размер списка: {}.", query, output.size());
        return output;
    }
}
