package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utility.exceptions.ShareItProvidedDataException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    @Autowired
    private ItemRequestService service;

    @GetMapping("/{id}")
    public ItemRequestDto getById(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                  @Positive @PathVariable Long id) {
        service.validateUser(userId);
        return service.get(id);
    }

    @GetMapping
    public List<ItemRequestDto> get(@RequestHeader(name = "X-Sharer-User-Id") Optional<Long> userId) {
        if (userId.isEmpty()) {
            return service.getAll();
        }
        return service.getAllForUser(userId.get());
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@Positive @RequestHeader(name = "X-Sharer-User-Id") Optional<Long> userId,
                                       @RequestParam(required = false, defaultValue = "0") long from,
                                       @Positive @RequestParam(required = false, defaultValue = "" + Integer.MAX_VALUE) int size) {
        // Как оказалось, @Positive - не работает... Буду разбираться, в чём проблема...
        if (from < 0 || size <= 0) {
            throw new ShareItProvidedDataException("Не верные данные для вывода страницы были введены.", "from=" + from + ",size=" + size);
        }
        if (userId.isPresent()) {
            return service.getAllWithPageExcludeUser(from, size, userId.get());
        }
        return service.getAllWithPage(from, size);
    }

    @PostMapping
    public ItemRequestDto post(@RequestBody ItemRequestDto request,
                               @Valid @Positive @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        UserDto newUser = new UserDto();
        newUser.setId(userId);
        request.setUser(newUser);
        return service.post(request);
    }

    @PatchMapping("/{id}")
    public ItemRequestDto update(@Positive @PathVariable long id, @RequestBody ItemRequestDto request,
                                 @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        UserDto newUser = new UserDto();
        newUser.setId(userId);
        request.setUser(newUser);
        request.setId(id);
        return service.update(request);
    }
}
