package ru.practicum.shareit.gateway.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;

import javax.validation.constraints.Positive;
import java.util.Optional;

@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private ItemClient client;

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@Positive @PathVariable Long id, @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        return client.getItem(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@Positive @RequestHeader(name = "X-Sharer-User-Id") Optional<Long> ownerId) {
        return client.getItems(ownerId.orElse(null)); // Знаю костыльно, но так надо, дабы соответствовать коду сервера.
    }

    @PostMapping
    public ResponseEntity<Object> upload(@RequestBody ItemDto item, @Positive @RequestHeader(name = "X-Sharer-User-Id") Long ownerId) {
        return client.postItem(ownerId, item);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@Positive @PathVariable long id, @RequestBody ItemDto item,
                                         @RequestHeader(name = "X-Sharer-User-Id") Long ownerId) {
        return client.updateItem(ownerId, id, item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@Positive @PathVariable long id) {
        return client.deleteItem(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getSearchedItems(@RequestParam String text) {
        return client.searchItem(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComments(@Positive @PathVariable long itemId, @RequestBody CommentDto commentDto,
                                               @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        return client.postComment(userId, itemId, commentDto);
    }
}
