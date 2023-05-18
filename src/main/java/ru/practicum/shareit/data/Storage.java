package ru.practicum.shareit.data;

import java.util.List;

public interface Storage<T> {
    T get(long id);
    List<T> getAll();

    T upload(T obj);
    T update(T obj);
    T delete(long id);

    // Это задел на будущее, например если нужно будет получить отсортированный список,
    // сделать специфичное действие или что-то в этом роде.
    List<T> specialGet(String[] args);
    void specialAction(String[] args);
}
