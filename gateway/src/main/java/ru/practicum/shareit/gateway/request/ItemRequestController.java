package ru.practicum.shareit.gateway.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Optional;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    @Autowired
    private ItemRequestClient client;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                          @Positive @PathVariable Long id) {
        return client.getRequest(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> get(@RequestHeader(name = "X-Sharer-User-Id") Optional<Long> userId) {
        return client.getRequests(userId.orElse(null));
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@Positive @RequestHeader(name = "X-Sharer-User-Id") Optional<Long> userId,
                                         @RequestParam(required = false, defaultValue = "0") long from,
                                         @Positive @RequestParam(required = false, defaultValue = "" + Integer.MAX_VALUE) int size) {
        return client.getRequestsAll(userId.orElse(null), from, size);
    }

    @PostMapping
    public ResponseEntity<Object> post(@RequestBody ItemRequestDto request,
                                       @Valid @Positive @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        return client.postRequest(userId, request);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@Positive @PathVariable long id, @RequestBody ItemRequestDto request,
                                         @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        return client.updateRequest(id, userId, request);
    }
}
