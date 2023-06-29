package ru.practicum.shareit.gateway.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.user.dto.UserDto;
import ru.practicum.shareit.gateway.utility.BaseClient;
import ru.practicum.shareit.gateway.utility.exceptions.ShareItProvidedDataException;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postUser(UserDto body) {
        if (body.getName() == null) {
            throw new ShareItProvidedDataException("Имя должно быть указано.", body);
        }
        if (body.getEmail() == null) {
            throw new ShareItProvidedDataException("Почта должна быть указана.", body);
        }
        validateUser(body);
        return post("", body);
    }

    public ResponseEntity<Object> getUser(long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> getUsers() {
        return get("");
    }

    public ResponseEntity<Object> updateUser(long userId, UserDto body) {
        if (body != null) {
            validateUser(body);
        }
        return patch("/" + userId, body);
    }

    public ResponseEntity<Object> deleteUser(long userId) {
        return delete("/" + userId);
    }

    private void validateUser(UserDto user) {
        if (user.getName() != null) {
            if (user.getName().isBlank()) {
                throw new ShareItProvidedDataException("Имя не должно быть пустым.", user);
            }
        }
        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                throw new ShareItProvidedDataException("Почта не должна быть пустой.", user);
            }
        }
        // Остальное в ДТОшке роли не играет при отправке запроса.
    }
}