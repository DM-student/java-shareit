package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.data.ItemDataBaseStorage;
import ru.practicum.shareit.data.UserDataBaseStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utility.JavaxValidationHandler;
import ru.practicum.shareit.utility.exceptions.ShareItNotFoundException;
import ru.practicum.shareit.utility.exceptions.ShareItValidationException;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
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

    public ItemDto get(long id) {
        Item item;
        try {
            item = storage.getById(id);
        }
        catch (EntityNotFoundException e) {
            throw new ShareItNotFoundException("Предмет не найден.", "item.id = " + id);
        }
        return mapper.mapToDto(item, true);
    }

    public List<ItemDto> getAll() {
        return storage.findAll().stream().map(item -> mapper.mapToDto(item, true)).collect(Collectors.toList());
    }

    public List<ItemDto> getAllForUser(Long id) {
        if(!userStorage.existsById(id)) {
            throw new ShareItNotFoundException("Пользователь не найден.", "user.id = " + id);
        }
        return storage.findAll().stream().filter(item -> Objects.equals(item.getOwnerId(), id))
                .map(item -> mapper.mapToDto(item, true)).collect(Collectors.toList());
    }

    public ItemDto upload(ItemDto item) {
        Item newItem = mapper.mapFromDto(item);
        if (!validation.validate(newItem)) {
            throw new ShareItValidationException("Предмет не прошёл валидацию.",
                    validation.validateFull(newItem));
        }
        if(userStorage.findById(newItem.getOwnerId()).isEmpty()) {
            throw new ShareItNotFoundException("Был указан несуществующий владелец.", newItem);
        }
        item.setId(null);
        return mapper.mapToDto(storage.save(newItem), true);
    }

    public ItemDto update(ItemDto item) {
        Item newItem = mapper.mapFromDto(item);
        Item itemToUpdate;

        try {
            itemToUpdate = storage.getById(newItem.getId());
        }
        catch (EntityNotFoundException e) {
            throw new ShareItNotFoundException("Предмет не найден.", item);
        }
        long oldOwnerId = itemToUpdate.getOwnerId();
        itemToUpdate.mergeFrom(newItem);

        if (!validation.validate(itemToUpdate)) {
            throw new ShareItValidationException("Предмет не прошёл валидацию.",
                    validation.validateFull(itemToUpdate));
        }

        if (!Objects.equals(oldOwnerId, newItem.getOwnerId())) {
            throw new ShareItNotFoundException("При обновлении предмета указан новый пользователь.", item);
        }
        return mapper.mapToDto(storage.save(itemToUpdate), true);
    }

    public ItemDto delete(long id) {
        ItemDto deletedItem = get(id);
        storage.deleteById(id);
        return deletedItem;
    }

    public List<ItemDto> getSearched(String query) {
        return storage.findAll().stream().filter(item -> item.getName().toLowerCase().equals(query.toLowerCase()))
                .map(item -> mapper.mapToDto(item, true)).collect(Collectors.toList());
    }
}
