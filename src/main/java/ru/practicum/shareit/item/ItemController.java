package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.awt.color.ProfileDataException;
import java.util.List;
import java.util.Optional;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @GetMapping("/{id}")
    public ItemDto get(@PathVariable Long id) {
        return itemService.get(id);
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(name = "X-Sharer-User-Id") Optional<Long> ownerId) {
        if (ownerId.isPresent()) {
            return itemService.getAllForUser(ownerId.get());
        }
        return itemService.getAll();
    }

    @PostMapping
    public ItemDto upload(@RequestBody ItemDto item, @RequestHeader(name = "X-Sharer-User-Id") Long ownerId) {
        item.setOwner(new UserDto(ownerId, null, null, false));
        return itemService.upload(item);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@PathVariable long id, @RequestBody ItemDto item,
                          @RequestHeader(name = "X-Sharer-User-Id") Long ownerId) {
        if (ownerId != null) {
            item.setOwner(new UserDto(ownerId, null, null, false));
        }
        item.setId(id);
        return itemService.update(item);
    }

    @DeleteMapping("/{id}")
    public ItemDto delete(@PathVariable long id) {
        return itemService.delete(id);
    }

    @GetMapping("/search")
    public List<ItemDto> getSearchedItems(@RequestParam String text) {
        // Оно как бы передаёт айди, как я понял, того кто ищет. Я вот не уверен,
        // стоит ли это логировать и/или делать обязательным для указания при
        // запросе по этому пути.
        return itemService.getSearched(text);

    }
}
