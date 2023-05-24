package ru.practicum.shareit.data.storage_implementation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.data.Storage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.utility.exceptions.ShareItNotFoundException;

import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Component
public class ItemStorage implements Storage<Item> {

    private final JdbcTemplate jdbcTemplate;

    private Long getItemOwnerId(long itemId) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select user_id from " +
                "users_to_items where item_id = ?", itemId);
        if (sqlRows.next()) {
            return sqlRows.getLong("user_id");
        }
        delete(itemId); // Удаляем бесхозный предмет
        throw new NullPointerException("Владелец для Item#" + itemId + " не найден в базе данных.");
        // Вообще, по-хорошему, предмет не должен быть бесхозным, но если такое случится - он будет удалён,
        // а после последует выброс исключения.
    }

    private void setItemOwner(long ownerId, long itemId) {
        String sqlQuery = "insert into users_to_items" +
                "(user_id, item_id) " +
                "values (?, ?)";
        try {
            jdbcTemplate.update(sqlQuery, ownerId, itemId);
        } catch (DuplicateKeyException e) {
            // Это исключение должно и будет постоянно выскакивать, ибо там стоит условие unique,
            // которое не даёт создать дубликат. За сим обработка его не требуется. Банально выскакивая - оно
            // уже выполняет задачу по ограничению добавления дубликата.
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
            throw new ShareItNotFoundException("Предмет не найден.", "id#" + id);
        }
    }

    private List<Item> getAllForUser(long userId) {
        Set<Long> itemsIds = getUserItemsIds(userId);
        List<Item> items = new ArrayList<>();
        for (long id : itemsIds) {
            items.add(get(id));
        }
        return items;
    }

    private List<Item> getAllForSearchQuery(String query, boolean ignoreAvailable) {
        if (query == null || getTextForSearch(query).isBlank()) {
            return Collections.emptyList();
        }
        String modifiedQuery = "%" + getTextForSearch(query) + "%";
        String sqlQuery = "select id " +
                "from items " +
                "where (name_for_searching like ? " +
                "or description_for_searching like ?) ";
        if (!ignoreAvailable) {
            sqlQuery += "and available = true";
        }
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet(sqlQuery, modifiedQuery, modifiedQuery);
        Set<Long> itemsIds = new HashSet<>();
        while (sqlRows.next()) {
            itemsIds.add(sqlRows.getLong("id"));
        }
        List<Item> items = new ArrayList<>();
        for (long id : itemsIds) {
            items.add(get(id));
        }
        return items;
    }

    private String getTextForSearch(String text) {
        return text.toLowerCase().replaceAll("[^а-яa-z]", "");
    }


    // Мне намного проще портировать этот небольшой метод пока-что.
    private Set<Long> getUserItemsIds(long userId) {
        Set<Long> items = new HashSet<>();
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("select item_id from " +
                "users_to_items where user_id = ?", userId);
        while (sqlRows.next()) {
            items.add(sqlRows.getLong("item_id"));
        }
        log.info("Возвращён полный список предметов пользователя ID#{} размером: {}", userId, items.size());
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
    public Item create(Item obj) {
        String sqlQuery = "insert into items" +
                "(name, name_for_searching, description, description_for_searching, available) " +
                "values (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sqlQuery, new String[]{"id"});
            ps.setString(1, obj.getName());
            ps.setString(2, getTextForSearch(obj.getName()));
            ps.setString(3, obj.getDescription());
            ps.setString(4, getTextForSearch(obj.getDescription()));
            ps.setBoolean(5, obj.getAvailable());
            return ps;
        }, keyHolder);

        obj.setId((long) keyHolder.getKey());

        if (obj.getOwnerId() != null) {
            setItemOwner(obj.getOwnerId(), obj.getId());
        }

        log.info("Загружен новый предмет в БД: id = {}, name = \"{}\"", obj.getId(), obj.getName());
        return obj;
    }

    @Override
    public Item update(Item obj) {
        String sqlQuery = "update items set " +
                "name = ?, name_for_searching = ?, description = ?, description_for_searching = ?, available = ? " +
                "where id = ?";
        int affectedRows = jdbcTemplate.update(sqlQuery, obj.getName(), getTextForSearch(obj.getName()),
                obj.getDescription(), getTextForSearch(obj.getDescription()), obj.getAvailable(), obj.getId());
        if (affectedRows == 0) {
            throw new ShareItNotFoundException("Предмет не найден.", "id#" + obj.getId());
        }
        if (obj.getOwnerId() != null) {
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
        switch (argsList.get(0)) {
            case "user":
                return getAllForUser(Long.parseLong(argsList.get(1)));
            case "search":
                boolean ignoreAvailable = false;
                if (argsList.size() > 2) {
                    ignoreAvailable = Objects.equals(argsList.get(2), "ignoreAvailable");
                }
                return getAllForSearchQuery(argsList.get(1), ignoreAvailable);
            default:
                throw new IllegalArgumentException("specialGet получил неверный ключ операции.");
        }
    }


    @Autowired
    public ItemStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
