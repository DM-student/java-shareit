package ru.practicum.shareit.gateway.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;
import ru.practicum.shareit.gateway.utility.BaseClient;
import ru.practicum.shareit.gateway.utility.exceptions.ShareItProvidedDataException;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postItem(long userId, ItemDto body) {
        if (body.getName() == null) {
            throw new ShareItProvidedDataException("Название должно быть указано.", body);
        }
        if (body.getDescription() == null) {
            throw new ShareItProvidedDataException("Описание должно быть указано.", body);
        }
        validateItem(body);
        return post("", userId, body);
    }

    public ResponseEntity<Object> getItem(long userId, long ItemId) {
        return get("/" + ItemId, userId);
    }

    public ResponseEntity<Object> getItems(Long userId) {
        if (userId == null) {
            get("");
        }
        return get("", userId);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, ItemDto body) {
        if (body != null) {
            validateItem(body);
        }
        return patch("/{id}", userId, Map.of("id", itemId), body);
    }

    public ResponseEntity<Object> deleteItem(long ItemId) {
        return delete("/" + ItemId);
    }

    public ResponseEntity<Object> searchItem(String query) {
        return get("/search?text=" + query);
    }

    public ResponseEntity<Object> postComment(long userId, long itemId, CommentDto body) {
        // Раздельно, ибо использование && не спасёт от НуллПоинтера при попытке вызвать isBlank(),
        // по крайней мере на моей практике это было так.
        if (body.getText() == null) {
            throw new ShareItProvidedDataException("Текст комментария не должен быть пустым.", body);
        }
        if (body.getText().isBlank()) {
            throw new ShareItProvidedDataException("Текст комментария не должен быть пустым.", body);
        }
        return post("/{id}/comment", userId, Map.of("id", itemId), body);
    }

    private void validateItem(ItemDto item) {
        if (item.getId() != null) {
            if (item.getId() <= 0) {
                throw new ShareItProvidedDataException("ID должен быть больше нуля.", item);
            }
        }
        if (item.getRequestId() != null) {
            if (item.getRequestId() <= 0) {
                throw new ShareItProvidedDataException("ID заявки должен быть больше нуля.", item);
            }
        }
        if (item.getName() != null) {
            if (item.getName().isBlank()) {
                throw new ShareItProvidedDataException("Название не должно быть пустым.", item);
            }
        }
        if (item.getDescription() != null) {
            if (item.getDescription().isBlank()) {
                throw new ShareItProvidedDataException("Описание не должно быть пустым.", item);
            }
        }
        // Остальное в ДТОшке роли не играет при отправке запроса.
    }
}