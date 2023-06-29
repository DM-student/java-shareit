package ru.practicum.shareit.gateway.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.request.dto.ItemRequestDto;
import ru.practicum.shareit.gateway.utility.BaseClient;
import ru.practicum.shareit.gateway.utility.exceptions.ShareItProvidedDataException;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postRequest(long userId, ItemRequestDto body) {
        if (body.getDescription() == null) {
            throw new ShareItProvidedDataException("Содержание заявки должно быть указано.", body);
        }
        if (body.getDescription().isBlank()) {
            throw new ShareItProvidedDataException("Содержание заявки должно быть указано.", body);
        }
        return post("", userId, body);
    }

    public ResponseEntity<Object> getRequest(long userId, long id) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> getRequests(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getRequestsAll(Long userId, long from, int size) {
        Map<String, Object> parameters =
                Map.of("from", from,
                        "size", size);
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> updateRequest(long id, long userId, ItemRequestDto body) {
        if (body != null) {
            if (body.getDescription().isBlank()) {
                throw new ShareItProvidedDataException("Содержание заявки не должно быть пустым.", body);
            }
        }
        return patch("/" + id, userId, body);
    }

    public ResponseEntity<Object> deleteRequest(long id) {
        return delete("/" + id);
    }
}