package ru.practicum.shareit.data.storage_implementation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.data.Storage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utility.exceptions.ShareItConflictException;
import ru.practicum.shareit.utility.exceptions.ShareItNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserStorage implements Storage<User> {

    private final Storage<Item> itemStorage;
    private final JdbcTemplate jdbcTemplate;

    private long lastId = 0;

    private Set<Long> getUserItems(long userId) {
        Set<Long> items = new HashSet<>();
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select item_id from " +
                "users_to_items where user_id = ?", userId);
        while (sqlRows.next()) {
            items.add(sqlRows.getLong("item_id"));
        }
        return items;
    }

    private void uploadUserItems(long userId, Set<Long> items) {
        String sqlQuery = "insert into users_to_items" +
                "(user_id, item_id) " +
                "values (?, ?)";
        for (long item : items) {
            try {
                jdbcTemplate.update(sqlQuery, userId, item);
            } catch (DuplicateKeyException e) {
                // Это исключение должно и будет постоянно выскакивать, ибо там стоит условие unique,
                // которое не даёт создать дубликат. За сим обработка его не требуется. Банально выскакивая - оно
                // уже выполняет задачу по ограничению добавления дубликата.
            }
        }
    }

    private void removeUserItems(long userId) {
        String sqlQuery = "delete from users_to_items where user_id = ?";
        jdbcTemplate.update(sqlQuery, userId);
    }

    @Override
    public User get(long id) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select * from users where id = ?", id);

        // обрабатываем результат выполнения запроса
        if (sqlRows.next()) {
            User newUser = new User(
                    sqlRows.getLong("id"),
                    sqlRows.getString("name"),
                    sqlRows.getString("email")
            );
            newUser.getItemsIds().addAll(getUserItems(id));
            log.info("Найден пользователь в БД: id = {}, email = \"{}\"", newUser.getId(), newUser.getEmail());
            return newUser;
        } else {
            throw new ShareItNotFoundException("Пользователь не найден.", "id#" + id);
        }

    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();

        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select * from users");

        // обрабатываем результат выполнения запроса
        while (sqlRows.next()) {
            User newUser = new User(
                    sqlRows.getLong("id"),
                    sqlRows.getString("name"),
                    sqlRows.getString("email")
            );
            // Я не представляю, как тут всё реализовать одним запросом.
            newUser.getItemsIds().addAll(getUserItems(newUser.getId()));
            log.info("Найден пользователь в БД: id = {}, email = \"{}\"", newUser.getId(), newUser.getEmail());
            users.add(newUser);
        }
        log.info("Возвращён полный список пользователей размером: {}", users.size());
        return users;
    }

    public List<User> getByEmail(String email) {
        List<User> users = new ArrayList<>();

        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select * from users where email = ?", email);

        // обрабатываем результат выполнения запроса
        while (sqlRows.next()) {
            User newUser = new User(
                    sqlRows.getLong("id"),
                    sqlRows.getString("name"),
                    sqlRows.getString("email")
            );
            newUser.getItemsIds().addAll(getUserItems(newUser.getId()));
            log.info("Найден пользователь в БД по адресу электронной почты: id = {}, email = \"{}\"", newUser.getId(), newUser.getEmail());
            users.add(newUser);
        }
        log.info("Возвращён список пользователей c параметром email=\"{}\"", email);
        return users;
    }

    @Override
    public User create(User obj) {
        if (!getByEmail(obj.getEmail()).isEmpty()) {
            throw new ShareItConflictException("Пользователь с данным адресом " +
                    "электронной почты уже есть в БД.", "Предоставленный объект: " + obj);
        }

        String sqlQuery = "insert into users" +
                "(id, name, email) " +
                "values (?, ?, ?)";
        obj.setId(lastId + 1);
        jdbcTemplate.update(sqlQuery, obj.getId(), obj.getName(), obj.getEmail());
        uploadUserItems(obj.getId(), obj.getItemsIds());
        lastId++;
        log.info("Загружен новый пользователь в БД: id = {}, email = \"{}\"", obj.getId(), obj.getEmail());
        return obj;
    }

    @Override
    public User update(User obj) {
        if (!getByEmail(obj.getEmail()).isEmpty()) {
            if (!Objects.equals(getByEmail(obj.getEmail()).get(0).getId(), obj.getId())) {
                throw new ShareItConflictException("Пользователь с данным адресом " +
                        "электронной почты уже есть в БД.", "Предоставленный объект: " + obj);
            }
        }
        get(obj.getId());

        String sqlQuery = "update users set " +
                "name = ?, email = ?" +
                "where id = ?";
        jdbcTemplate.update(sqlQuery, obj.getName(),
                obj.getEmail(), obj.getId());
        uploadUserItems(obj.getId(), obj.getItemsIds());
        log.info("Обновлён пользователь в БД: id = {}, email = \"{}\"", obj.getId(), obj.getName());
        return obj;
    }

    @Override
    public User delete(long id) {
        // Как я понимаю, если пользователь удалён - нужно удалить все его вещи.
        List<Long> items = itemStorage.getAll().stream().map(Item::getId).collect(Collectors.toList());
        for (long item : items) {
            itemStorage.delete(item);
        }

        User deletedItem = get(id);
        String sqlQuery = "delete from users where id = ?";
        jdbcTemplate.update(sqlQuery, id);
        log.info("Удалён пользователь из БД: id = {}, name = \"{}\"", deletedItem.getId(), deletedItem.getEmail());
        return deletedItem;
    }

    @Override
    public List<User> specialGet(String[] args) {
        List<String> argsList = Arrays.asList(args);
        switch (argsList.get(0)) {
            case "email":
                if (argsList.get(1) == null) {
                    return null;
                }
                return getByEmail(argsList.get(1));
            default:
                throw new IllegalArgumentException("specialGet получил неверные аргументы.");
        }
    }

    @Autowired
    public UserStorage(JdbcTemplate jdbcTemplate, Storage<Item> itemStorage) {
        this.itemStorage = itemStorage;
        this.jdbcTemplate = jdbcTemplate;
    }
}
