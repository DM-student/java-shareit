package ru.practicum.shareit.data.storage_implementation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.data.Storage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.utility.exceptions.NotFoundExceptionForHandler;

import java.util.*;

@Slf4j
@Component
public class ItemStorage implements Storage<Item> {

    private final JdbcTemplate jdbcTemplate;

    private long lastId = 0;

    private Long getItemOwnerId(long itemId) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select user_id from " +
                "users_to_items where item_id = ?", itemId);
        if(sqlRows.next()) {
            return sqlRows.getLong("user_id");
        }
        return null;
    }

    private void setItemOwner(long ownerId, long itemId) {
        String sqlQuery = "insert into users_to_items" +
                "(user_id, item_id) " +
                "values (?, ?)";
        try {
            jdbcTemplate.update(sqlQuery, ownerId, itemId);
        } catch (DuplicateKeyException e) {
            // Nothing really.
        }

    }

    private void removeItemOwner(long ownerId, long itemId) {
        String sqlQuery = "delete from users_to_items where user_id = ? AND item_id = ?";
        jdbcTemplate.update(sqlQuery, ownerId, itemId);
    }

    @Override
    public Item get(long id) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select * from items where id = ?", id);

        // обрабатываем результат выполнения запроса
        if (sqlRows.next()) {
            Item newItem = new Item(
                    sqlRows.getLong("id"),
                    null,
                    sqlRows.getString("name"),
                    sqlRows.getString("description"),
                    sqlRows.getBoolean("available")
                    );
            newItem.setOwnerId(getItemOwnerId(newItem.getId()));
            log.info("Найден предмет в БД: id = {}, name = \"{}\"", newItem.getId(), newItem.getName());
            return newItem;
        } else {
            throw new NotFoundExceptionForHandler("Предмет не найден.", "id#" + id);
        }
    }

    private List<Item> getAllForUser(long userId) {
        Set<Long> itemsIds = getUserItemsIds(userId);
        List<Item> items = new ArrayList<>();
        for(long id : itemsIds) {
            items.add(get(id));
        }
        return items;
    }

    // Мне намного проще портировать этот небольшой метод пока-что.
    private Set<Long> getUserItemsIds(long userId) {
        Set<Long> items = new HashSet<>();
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select item_id from " +
                "users_to_items where user_id = ?", userId);
        while(sqlRows.next()) {
            items.add(sqlRows.getLong("item_id"));
        }
        log.info("Возвращён полный список предметов пользователя ID#{} размером: {}",userId, items.size());
        return items;
    }

    @Override
    public List<Item> getAll() {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select * from items");
        List<Item> items = new ArrayList<>();

        // обрабатываем результат выполнения запроса
        while (sqlRows.next()) {
            Item newItem = new Item(
                    sqlRows.getLong("id"),
                    getItemOwnerId(sqlRows.getLong("id")),
                    sqlRows.getString("name"),
                    sqlRows.getString("description"),
                    sqlRows.getBoolean("available")
            );
            items.add(newItem);
        }
        log.info("Возвращён полный список предметов размером: {}", items.size());
        return items;
    }

    @Override
    public Item upload(Item obj) {
        String sqlQuery = "insert into items" +
                "(id, name, description, available) " +
                "values (?, ?, ?, ?)";
        obj.setId(lastId + 1);
        jdbcTemplate.update(sqlQuery, obj.getId(), obj.getName(),
                obj.getDescription(), obj.getAvailable());
        if(obj.getOwnerId() != null) {
            setItemOwner(obj.getOwnerId(), obj.getId());
        }
        lastId++;
        log.info("Загружен новый предмет в БД: id = {}, name = \"{}\"", obj.getId(), obj.getName());
        return obj;
    }

    @Override
    public Item update(Item obj) {
        get(obj.getId());

        String sqlQuery = "update items set " +
                "name = ?, description = ?, available = ? " +
                "where id = ?";
        jdbcTemplate.update(sqlQuery, obj.getName(),
                obj.getDescription(), obj.getAvailable(), obj.getId());
        if(obj.getOwnerId() != null) {
            setItemOwner(obj.getOwnerId(), obj.getId());
        }
        log.info("Обновлён предмет в БД: id = {}, name = \"{}\"", obj.getId(), obj.getName());
        return obj;
    }
    @Override
    public Item delete(long id) {
        Item deletedItem = get(id);
        String sqlQuery = "delete from items where id = ?";
        jdbcTemplate.update(sqlQuery, id);
        log.info("Удалён предмет из БД: id = {}, name = \"{}\"", deletedItem.getId(), deletedItem.getName());
        return deletedItem;
    }

    @Override
    public List<Item> specialGet(String[] args) {
        List<String> argsList = Arrays.asList(args);
        switch(argsList.get(0)) {
            case "user":
                if(argsList.get(1) == null) {
                    throw new IllegalArgumentException("specialGet получил null в качестве второго аргумента.");
                }
                return getAllForUser(Long.parseLong(argsList.get(1)));
            default:
                throw new IllegalArgumentException("specialGet получил неверные аргументы.");
        }
    }

    @Override
    public void specialAction(String[] args) {

    }

    @Autowired
    public ItemStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
