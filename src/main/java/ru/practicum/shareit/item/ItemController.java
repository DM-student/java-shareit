package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @GetMapping("/{id}")
    public ItemDto get(@PathVariable Long id, @RequestHeader(name = "X-Sharer-User-Id") Optional<Long> userId) {
        return itemService.get(id, userId.get());
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(name = "X-Sharer-User-Id") Optional<Long> ownerId) {
        if (ownerId.isPresent()) {
            return itemService.getAllForUser(ownerId.get());
        }
        return itemService.getAll(ownerId.get());
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
        if (text.isBlank()) {
            return List.of();
        }
        return itemService.getSearchedAvailable(text);

    }

    @PostMapping("/{itemId}/comment")
    public CommentDto getComments(@PathVariable long itemId, @RequestBody CommentDto commentDto,
                                        @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        Comment comment = new Comment();
        comment.setItemId(itemId);
        comment.setText(commentDto.getText());
        comment.setUserId(userId);
        return itemService.postComment(comment);
    }
}
